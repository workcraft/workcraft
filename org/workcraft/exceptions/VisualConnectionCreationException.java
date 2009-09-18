package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class VisualConnectionCreationException extends Exception {

	public VisualConnectionCreationException() {
		super();
	}

	public VisualConnectionCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public VisualConnectionCreationException(String message) {
		super(message);
	}

	public VisualConnectionCreationException(Throwable cause) {
		super(cause);
	}
}
