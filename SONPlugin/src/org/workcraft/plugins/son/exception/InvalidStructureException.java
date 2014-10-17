package org.workcraft.plugins.son.exception;

public class InvalidStructureException extends Exception{
	private static final long serialVersionUID = 1L;

	public InvalidStructureException(String msg){
		super("Fail to run SON simulator, error may due to incorrect structure ("+ msg + ").");
	}
}
