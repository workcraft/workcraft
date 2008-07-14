package org.workcraft.framework.exceptions;
// :P
public class ModelLoadFailedException extends Exception{
	private static final long serialVersionUID = 1L;

	public ModelLoadFailedException(String reason) {
		super(reason);
	}
}
