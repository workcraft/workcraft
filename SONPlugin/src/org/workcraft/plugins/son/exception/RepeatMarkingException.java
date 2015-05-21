package org.workcraft.plugins.son.exception;

public class RepeatMarkingException extends Exception{

	private static final long serialVersionUID = 1L;

	public RepeatMarkingException(String msg){
		super("token amount > 1 +" + msg);
	}
}
