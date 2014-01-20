/*
 *      Copyright (C) 2005-2015 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.jsonrpc.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.io.ApiException;
import org.xbmc.android.jsonrpc.io.ConnectionManager;
import org.xbmc.android.jsonrpc.io.JsonHandler;
import org.xbmc.android.jsonrpc.notification.AbstractEvent;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Service which keeps a steady TCP connection to XBMC's JSON-RPC API via TCP
 * socket (as opposed to HTTP messages).
 * <p/>
 * It serves as listener for notification, but is also used for posting normal
 * API requests.
 * <p/>
 * Generally speaking, the service will shut down and terminate the TCP
 * connection as soon as there are no more connected clients. However, clients
 * may want to query several consecutive requests without having the service
 * stop and restart between every request. That is the reason why there is a
 * "cooldown" period, in which the service will just wait for new clients to
 * arrive before shutting down.
 * <p/>
 * About message exchange, see {@link ConnectionManager}.
 *
 * @author freezy <freezy@xbmc.org>
 */
public class ConnectionService extends IntentService {

	public final static String TAG = ConnectionService.class.getSimpleName();

	private static final int SOCKET_TIMEOUT = 5000;

	public static final String EXTRA_ADDRESS = "org.xbmc.android.jsonprc.extra.ADDRESS";
	public static final String EXTRA_TCPPORT = "org.xbmc.android.jsonprc.extra.TCPPORT";
	public static final String EXTRA_HTTPPORT = "org.xbmc.android.jsonprc.extra.HTTPPORT";

	public static final String EXTRA_STATUS = "org.xbmc.android.jsonprc.extra.STATUS";
	public static final String EXTRA_APICALL = "org.xbmc.android.jsonprc.extra.APICALL";
	public static final String EXTRA_NOTIFICATION = "org.xbmc.android.jsonprc.extra.NOTIFICATION";
	public static final String EXTRA_HANDLER = "org.xbmc.android.jsonprc.extra.HANDLER";
	public static final String EXTRA_CALLID = "org.xbmc.android.jsonprc.extra.CALLID";

	public static final int MSG_REGISTER_CLIENT = 0x01;
	public static final int MSG_UNREGISTER_CLIENT = 0x02;
	public static final int MSG_CONNECTING = 0x03;
	public static final int MSG_CONNECTED = 0x04;
	public static final int MSG_RECEIVE_NOTIFICATION = 0x05;
	public static final int MSG_RECEIVE_APICALL = 0x06;
	public static final int MSG_RECEIVE_HANDLED_APICALL = 0x07;
	public static final int MSG_SEND_APICALL = 0x08;
	public static final int MSG_SEND_HANDLED_APICALL = 0x09;
	public static final int MSG_ERROR = 0x0a;

	public static final int RESULT_SUCCESS = 0x01;

	/**
	 * Time in milliseconds we wait for new requests until we shut down the
	 * service (and connection).
	 */
	private static final long COOLDOWN = 10000;

	/**
	 * Static reference to Jackson's object mapper.
	 */
	private final static ObjectMapper OM = new ObjectMapper();

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	private final Messenger messenger = new Messenger(new IncomingHandler());

	/**
	 * Keeps track of all currently registered client. Normally, all clients
	 * are {@link ConnectionManager} instances.
	 */
	private final ArrayList<Messenger> clients = new ArrayList<Messenger>();
	/**
	 * API call results are only returned to the client requested it, so here are the relations.
	 */
	private final HashMap<String, Messenger> clientMap = new HashMap<String, Messenger>();
	/**
	 * If we have to send data before we're connected, store data until connection
	 */
	private final LinkedList<AbstractCall<?>> pendingCalls = new LinkedList<AbstractCall<?>>();
	/**
	 * All calls the service is currently dealing with. Key is the ID of the call.
	 */
	private final HashMap<String, AbstractCall<?>> calls = new HashMap<String, AbstractCall<?>>();
	/**
	 * The handler we'll update with a status code as soon as we're done.
	 */
	private final HashMap<String, JsonHandler> handlers = new HashMap<String, JsonHandler>();

	/**
	 * Reference to the socket, so we shut it down properly.
	 */
	private Socket socket = null;
	/**
	 * Output writer so we can also write stuff to the socket.
	 */
	private BufferedWriter out = null;

	/**
	 * When no more clients are connected, wait {@link #COOLDOWN} milliseconds
	 * and then shut down the service if no new clients connect.
	 */
	private Timer cooldownTimer = null;

	/**
	 * Class constructor must be empty for services.
	 */
	public ConnectionService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		final int port = intent.getIntExtra(EXTRA_TCPPORT, 9090);
		final String address = intent.getStringExtra(EXTRA_ADDRESS) != null ? intent.getStringExtra(EXTRA_ADDRESS) : "10.0.2.2";

		long s = System.currentTimeMillis();
		Log.d(TAG, "Starting TCP client for " + address + ":" + port + "...");
		notifyStatus(MSG_CONNECTING, null);
		Socket sock;

		try {
			final InetSocketAddress sockaddr = new InetSocketAddress(address, port);
			sock = new Socket();
			socket = sock;       // update class reference
			sock.setSoTimeout(0); // no timeout for reading from connection.
			sock.connect(sockaddr, SOCKET_TIMEOUT);
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			Log.d(TAG, "Connected and writer setup.");
		} catch (UnknownHostException e) {
			Log.e(TAG, "Unknown host: " + e.getMessage(), e);
			notifyError(new ApiException(ApiException.IO_UNKNOWN_HOST,  "Unknown host: " + e.getMessage(), e), null);
			stopSelf();
			out = null;
			return;
		} catch (SocketTimeoutException e) {
			Log.e(TAG, "Unknown host: " + e.getMessage(), e);
			notifyError(new ApiException(ApiException.IO_SOCKETTIMEOUT,  "Connection timeout: " + e.getMessage(), e), null);
			stopSelf();
			out = null;
			return;
		} catch (IOException e) {
			Log.e(TAG, "I/O error while opening: " + e.getMessage(), e);
			notifyError(new ApiException(ApiException.IO_EXCEPTION_WHILE_OPENING,  "I/O error while opening: " + e.getMessage(), e), null);
			stopSelf();
			out = null;
			return;
		}

		try {
			Log.i(TAG, "Connected to TCP socket in " + (System.currentTimeMillis() - s) + "ms");
			notifyStatus(MSG_CONNECTED, null);

			// check for saved post data to send while we weren't connected, but
			// do it in a separate thread so we can read already while sending.
			if (!pendingCalls.isEmpty()) {
				new Thread("post-data-on-connection") {
					@Override
					public void run() {
						final LinkedList<AbstractCall<?>> calls = pendingCalls;
						while (!calls.isEmpty()) {
							writeSocket(calls.poll());
						}
					}
				}.start();
			}

			final JsonFactory jf = OM.getJsonFactory();
			final JsonParser jp = jf.createJsonParser(sock.getInputStream());
			JsonNode node;
			while ((node = OM.readTree(jp)) != null) {
				final String debugDump = ((Object)node).toString();
				if (debugDump.length() > 80) {
					Log.i(TAG, "READ: " + debugDump.substring(0, 80) + "...");
				} else {
					Log.i(TAG, "READ: " + debugDump);
				}
				notifyClients(node);
			}


		} catch (JsonParseException e) {
			Log.e(TAG, "Cannot parse JSON response: " + e.getMessage(), e);
			notifyError(new ApiException(ApiException.JSON_EXCEPTION,  "Error while parsing JSON response: " + e.getMessage(), e), null);
		} catch (EOFException e) {
			Log.i(TAG, "Connection broken, quitting.");
			notifyError(new ApiException(ApiException.IO_DISCONNECTED,  "Socket disconnected: " + e.getMessage(), e), null);
		} catch (IOException e) {
			Log.e(TAG, "I/O error while reading (" + e.getClass().getSimpleName() + "): " + e.getMessage(), e);
			notifyError(new ApiException(ApiException.IO_EXCEPTION_WHILE_READING,  "I/O error while reading: " + e.getMessage(), e), null);
		} finally {
			try {
				if (sock != null) {
					sock.close();
				}
				if (out != null) {
					out.close();
					out = null;
					Log.i(TAG, "TCP socket closed.");
				}
			} catch (IOException e) {
				// do nothing.
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "Connection service bound to new client.");
		return messenger.getBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			final Socket socket = this.socket;
			if (socket != null) {
				if (socket.isConnected()) {
					socket.shutdownInput();
				}
				if (!socket.isClosed()) {
					socket.close();
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Error closing socket.", e);
		}
		Log.d(TAG, "Notification service destroyed.");
	}

	/**
	 * Starts cooldown. If there is no new client for {@link #COOLDOWN}
	 * milliseconds, the service will shutdown, otherwise it will continue
	 * to run until there is another cooldown.
	 */
	private void cooldown() {
		Log.i(TAG, "Starting service cooldown.");
		cooldownTimer = new Timer();
		cooldownTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (clients.isEmpty()) {
					Log.i(TAG, "No new clients, shutting down service.");
					out = null;
					stopSelf();
				} else {
					Log.i(TAG, "Cooldown failed, got " + clients.size() + " new client(s).");
				}
			}
		}, COOLDOWN);
	}


	/**
	 * Treats the received result.
	 * <p/>
	 * If an ID is found in the response, the API call object is retrieved,
	 * updated and sent back to the client.
	 * <p/>
	 * Otherwise, the notification object is sent to all clients.
	 *
	 * @param node JSON-serialized response
	 */
	private void notifyClients(JsonNode node) {
		final ArrayList<Messenger> clients = this.clients;
		final HashMap<String, Messenger> map = clientMap;
		final HashMap<String, AbstractCall<?>> calls = this.calls;
		final HashMap<String, JsonHandler> handlers = this.handlers;

		// check for errors
		if (node.has("error")) {
			notifyError(new ApiException(node), node.get("id").getValueAsText());

			// check if notification or api call
		} else if (node.has("id")) {
			// it's api call.
			final String id = node.get("id").getValueAsText();
			if (calls.containsKey(id)) {
				final AbstractCall<?> call = calls.get(id);
				if (handlers.containsKey(id)) {
					// we got an provided handler, so apply it and send back status message.
					try {
						handlers.get(id).applyResult(node, getContentResolver());
						// get the right client to send back to
						if (map.containsKey(id)) {
							final Bundle b = new Bundle();
							b.putString(EXTRA_CALLID, call.getId());
							b.putInt(EXTRA_STATUS, RESULT_SUCCESS);
							final Message msg = Message.obtain(null, MSG_RECEIVE_HANDLED_APICALL);
							msg.setData(b);
							try {
								map.get(id).send(msg);
								Log.i(TAG, "API call handled successfully, posting status back to client.");
							} catch (RemoteException e) {
								Log.e(TAG, "Error posting status back to client: " + e.getMessage(), e);
								map.remove(id);
							} finally {
								// clean up
								map.remove(id);
								calls.remove(id);
							}
						} else {
							Log.w(TAG, "Cannot find client in caller-mapping for " + id + ", dropping response (handled call).");
						}
					} catch (ApiException e) {
						notifyError(e, id);
					}
				} else {
					// get the right client to send back to
					if (map.containsKey(id)) {
						call.setResponse(node);
						final Bundle b = new Bundle();
						b.putParcelable(EXTRA_APICALL, call);
						final Message msg = Message.obtain(null, MSG_RECEIVE_APICALL);
						msg.setData(b);
						try {
							map.get(id).send(msg);
							Log.i(TAG, "Sent updated API call " + call.getName() + " to client.");
						} catch (RemoteException e) {
							Log.e(TAG, "Error sending API response to client: " + e.getMessage(), e);
							map.remove(id);
						} finally {
							// clean up
							map.remove(id);
							calls.remove(id);
						}
					} else {
						Log.w(TAG, "Cannot find client in caller-mapping for " + id + ", dropping response (api call).");
					}
				}
			} else {
				Log.e(TAG, "Error: Cannot find API call with ID " + id + ".");
			}
		} else {
			// it's a notification.
			final AbstractEvent event = AbstractEvent.parse((ObjectNode) node);
			if (event != null) {
				Log.i(TAG, "Notifying " + clients.size() + " clients.");
				for (int i = clients.size() - 1; i >= 0; i--) {
					try {
						final Bundle b = new Bundle();
						b.putParcelable(EXTRA_NOTIFICATION, event);
						final Message msg = Message.obtain(null, MSG_RECEIVE_NOTIFICATION);
						msg.setData(b);
						clients.get(i).send(msg);

					} catch (RemoteException e) {
						Log.e(TAG, "Cannot send notification to client: " + e.getMessage(), e);
						/*
						 * The client is dead. Remove it from the list; we are
						 * going through the list from back to front so this is
						 * safe to do inside the loop.
						 */
						clients.remove(i);
						// stopSelf();
					}
				}
			} else {
				Log.i(TAG, "Ignoring unknown notification " + node.get("method").getTextValue() + ".");
			}
		}
	}

	/**
	 * Sends an error to all clients.
	 * @param e Thrown API exception
	 * @param id ID of the request
	 */
	private void notifyError(ApiException e, String id) {

		// if id is set and callback exists, only send error back to one client.
		if (id != null && clientMap.containsKey(id)) {
			try {
				final Message msg = Message.obtain(null, MSG_ERROR);
				msg.setData(e.getBundle(getResources()));
				clientMap.get(id).send(msg);
				Log.i(TAG, "Sent error to client with ID " + id + ".");
			} catch (RemoteException e2) {
				Log.e(TAG, "Cannot send errors to client " + id + ": "+ e.getMessage(), e2);
				clientMap.remove(id);
			}
		} else {
			// otherwise, send error back to all clients and die.
			for (int i = clients.size() - 1; i >= 0; i--) {
				final Message msg = Message.obtain(null, MSG_ERROR);
				msg.setData(e.getBundle(getResources()));
				try {
					clients.get(i).send(msg);
					Log.i(TAG, "Sent error to client " + i + ".");
				} catch (RemoteException e2) {
					Log.e(TAG, "Cannot send errors to client: " + e2.getMessage(), e2);
					/*
					 * The client is dead. Remove it from the list; we are going
					 * through the list from back to front so this is safe to do
					 * inside the loop.
					 */
					clients.remove(i);
				}
			}
			stopSelf();
			out = null;
		}
	}

	private void notifyStatus(int code, Messenger replyTo) {
		if (replyTo != null) {
			try {
				replyTo.send(Message.obtain(null, code));
			} catch (RemoteException e) {
				Log.e(TAG, "Could not notify sender of new status: " + e.getMessage(), e);
			}
		} else {
			for (int i = clients.size() - 1; i >= 0; i--) {
				final Message msg = Message.obtain(null, code);
				try {
					clients.get(i).send(msg);
				} catch (RemoteException e2) {
					Log.e(TAG, "Could not notify sender of new status: " + e2.getMessage(), e2);
					clients.remove(i);
				}
			}
		}
	}



	/**
	 * Serializes the API request and dumps it on the socket.
	 * @param call
	 */
	private void writeSocket(AbstractCall<?> call) {
		final String data = call.getRequest().toString();
		Log.d(TAG, "Sending data to server.");
		Log.d(TAG, "REQUEST: " + data);
		try {
			out.write(data + "\n");
			out.flush();
		} catch (IOException e) {
			Log.e(TAG, "Error writing to socket: " + e.getMessage(), e);
			notifyError(new ApiException(ApiException.IO_EXCEPTION_WHILE_WRITING,  "I/O error while writing: " + e.getMessage(), e),  call.getId());
		}
	}

	/**
	 * Handler of incoming messages from clients.
	 */
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_REGISTER_CLIENT:
					clients.add(msg.replyTo);
					Log.d(TAG, "Registered new client.");
					if (cooldownTimer != null) {
						Log.i(TAG, "Aborting cooldown timer.");
						cooldownTimer.cancel();
						cooldownTimer.purge();
					}
					if (socket != null && socket.isConnected()) {
						Log.d(TAG, "Directly notifying connected status.");
						notifyStatus(MSG_CONNECTED, msg.replyTo);
					}
					Log.d(TAG, "Done!");
					break;
				case MSG_UNREGISTER_CLIENT:
					clients.remove(msg.replyTo);
					Log.d(TAG, "Unregistered client.");
					if (clients.size() == 0) {
						Log.i(TAG, "No more clients, cooling down service.");
						cooldown();
					}
					break;
				case MSG_SEND_APICALL: {
					Log.d(TAG, "Sending new API call..");
					final Bundle data = msg.getData();
					final AbstractCall<?> call = data.getParcelable(EXTRA_APICALL);
					calls.put(call.getId(), call);
					clientMap.put(call.getId(), msg.replyTo);
					if (out == null) {
						pendingCalls.add(call);
					} else {
						writeSocket(call);
					}
				}
				break;
				case MSG_SEND_HANDLED_APICALL: {
					Log.d(TAG, "Sending new handled API call..");
					final Bundle data = msg.getData();
					final AbstractCall<?> call = data.getParcelable(EXTRA_APICALL);
					final JsonHandler handler = data.getParcelable(EXTRA_HANDLER);
					calls.put(call.getId(), call);
					handlers.put(call.getId(), handler);
					clientMap.put(call.getId(), msg.replyTo);
					if (out == null) {
						Log.d(TAG, "Quering for later.");
						pendingCalls.add(call);
					} else {
						writeSocket(call);
					}
				}
				break;
				default:
					super.handleMessage(msg);
			}
		}
	}
}
