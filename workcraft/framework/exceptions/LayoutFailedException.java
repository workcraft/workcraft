package org.workcraft.framework.exceptions;

public class LayoutFailedException extends Exception {
	public LayoutFailedException() {
		super();
	}

	public LayoutFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public LayoutFailedException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 1L;

	public LayoutFailedException(String message) {
		super(message);
	}
}
