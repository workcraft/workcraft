package org.workcraft.framework.exceptions;

@SuppressWarnings("serial")
public class ModelLoadFailedException extends Exception{

	public ModelLoadFailedException() {
		super();
	}

	public ModelLoadFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModelLoadFailedException(String message) {
		super(message);
	}

	public ModelLoadFailedException(Throwable cause) {
		super(cause);
	}

}
