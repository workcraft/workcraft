package org.workcraft.framework.exceptions;

public class DuplicateIdException extends Exception {
	private static final long serialVersionUID = 1L;

	public DuplicateIdException(Integer id) {
		super(id.toString());
	}
}
