package org.workcraft.exceptions;

public class ArgumentException extends RuntimeException
{
	private static final long serialVersionUID = 813881853034782201L;

	public ArgumentException() {
		super();
	}

	public ArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArgumentException(String message) {
		super(message);
	}

	public ArgumentException(Throwable cause) {
		super(cause);
	}
}
