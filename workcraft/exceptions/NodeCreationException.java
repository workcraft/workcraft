package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class NodeCreationException extends Exception {

	public NodeCreationException() {
		super();
	}

	public NodeCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeCreationException(String message) {
		super(message);
	}

	public NodeCreationException(Throwable cause) {
		super(cause);
	}
}
