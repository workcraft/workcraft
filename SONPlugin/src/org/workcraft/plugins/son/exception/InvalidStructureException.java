package org.workcraft.plugins.son.exception;

public class InvalidStructureException extends Exception{
	private static final long serialVersionUID = 1L;

	public InvalidStructureException(String msg){
		super("Invalid structure ("+ msg + ").");
	}
}
