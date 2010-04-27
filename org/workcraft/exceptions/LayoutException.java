package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class LayoutException extends RuntimeException {
	public LayoutException() {
		super();
	}

	public LayoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public LayoutException(String message) {
		super(message);
	}

	public LayoutException(Throwable cause) {
		super(cause);
	}
}
