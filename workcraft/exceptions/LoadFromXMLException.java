package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class LoadFromXMLException extends Exception{

	public LoadFromXMLException() {
		super();
	}

	public LoadFromXMLException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoadFromXMLException(String message) {
		super(message);
	}

	public LoadFromXMLException(Throwable cause) {
		super(cause);
	}

}
