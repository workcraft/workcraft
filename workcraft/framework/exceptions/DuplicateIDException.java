package org.workcraft.framework.exceptions;

public class DuplicateIDException extends Exception {
	private static final long serialVersionUID = 1L;

	public DuplicateIDException (int ID) {
		super (Integer.toString(ID));
	}
}
