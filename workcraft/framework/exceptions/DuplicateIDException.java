package org.workcraft.framework.exceptions;

public class DuplicateIDException extends Exception {
	private static final long serialVersionUID = 1L;

	public DuplicateIDException(String message) {
		super(message);
	}

	public DuplicateIDException(Integer ID) {
		super (ID.toString());
	}

}
