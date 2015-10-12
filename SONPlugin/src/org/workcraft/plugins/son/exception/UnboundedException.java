package org.workcraft.plugins.son.exception;

public class UnboundedException extends Exception{

	private static final long serialVersionUID = 1L;

	public UnboundedException(String msg){
		super("Occurrence net is unsafe: marking " + msg + " twice.");
	}
}
