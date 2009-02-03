package org.workcraft.framework.exceptions;

@SuppressWarnings("serial")
public class ModelLoadFailedException extends Exception{
	public ModelLoadFailedException(String reason) {
		super(reason);
	}
}
