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

package org.xbmc.android.jsonrpc.config;

/**
 * A set of configuration data needed to connect to an XBMC host.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class HostConfig {

	/**
	 * IP or host name of XBMC.
	 */
	public final String mAddress;
	/**
	 * HTTP port where JSON-RPC sits (defaults to 8080 on any platform but Windows, where it's 80).
	 */
	public final int mHttpPort;
	/**
	 * TCP port of JSON-RPC (defauls to 9090).
	 */
	public final int mTcpPort;
	/**
	 * Username, if authentication enabled.
	 */
	public final String mUsername;
	/**
	 * Password, if authentication enabled.
	 */
	public final String mPassword;
	
	/**
	 * Creates a new XBMC host object.
	 * @param address IP or host adress of XBMC
	 */
	public HostConfig(String address) {
		this(address, 8080);
	}
	
	/**
	 * Creates a new XBMC host object.
	 * @param address IP or host adress of XBMC
	 * @param httpPort HTTP port where JSON-RPC sits (defaults to 8080 on any platform but Windows, where it's 80).
	 */
	public HostConfig(String address, int httpPort) {
		this(address, httpPort, 9090);
	}

	/**
	 * Creates a new XBMC host object.
	 * @param address IP or host adress of XBMC
	 * @param httpPort HTTP port where JSON-RPC sits (defaults to 8080 on any platform but Windows, where it's 80).
	 * @param tcpPort TCP port of JSON-RPC (defauls to 9090).
	 */
	public HostConfig(String address, int httpPort, int tcpPort) {
		this(address, httpPort, tcpPort, null, null);
	}
	
	/**
	 * Creates a new XBMC host object.
	 * @param address IP or host adress of XBMC
	 * @param httpPort HTTP port where JSON-RPC sits (defaults to 8080 on any platform but Windows, where it's 80).
	 * @param username Username, if authentication enabled.
	 * @param password Password, if authentication enabled.
	 */
	public HostConfig(String address, int httpPort, String username, String password) {
		this(address, httpPort, 9090, username, password);
	}
	
	/**
	 * Creates a new XBMC host object.
	 * @param address IP or host adress of XBMC
	 * @param httpPort HTTP port where JSON-RPC sits (defaults to 8080 on any platform but Windows, where it's 80).
	 * @param tcpPort TCP port of JSON-RPC (defauls to 9090).
	 * @param username Username, if authentication enabled.
	 * @param password Password, if authentication enabled.
	 */
	public HostConfig(String address, int httpPort, int tcpPort, String username, String password) {
		mAddress = address;
		mHttpPort = httpPort;
		mTcpPort = tcpPort;
		mUsername = username;
		mPassword = password;
	}
	
	public String getAddress() {
		return mAddress;
	}

	public int getHttpPort() {
		return mHttpPort;
	}

	public int getTcpPort() {
		return mTcpPort;
	}

	public String getUsername() {
		return mUsername;
	}

	public String getPassword() {
		return mPassword;
	}
	
}
