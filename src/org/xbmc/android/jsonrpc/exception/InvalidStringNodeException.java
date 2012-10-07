package org.xbmc.android.jsonrpc.exception;

public class InvalidStringNodeException extends Exception {
	
	public InvalidStringNodeException(String message) {
		super(message);
	}
	
	public InvalidStringNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = -6319216221795443107L;
}
