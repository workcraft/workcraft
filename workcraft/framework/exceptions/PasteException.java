package org.workcraft.framework.exceptions;

@SuppressWarnings("serial")
public class PasteException extends Exception{
	public PasteException() {
		super();
	}
	public PasteException(String message, Throwable cause) {
		super(message, cause);
	}
	public PasteException(String message) {
		super(message);
	}
	public PasteException(Throwable cause) {
		super(cause);
	}
}
