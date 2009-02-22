package org.workcraft.framework.exceptions;

@SuppressWarnings("serial")
public class ComponentCreationException extends Exception {

	public ComponentCreationException() {
		super();
	}

	public ComponentCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ComponentCreationException(String message) {
		super(message);
	}

	public ComponentCreationException(Throwable cause) {
		super(cause);
	}
}
