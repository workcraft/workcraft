package org.workcraft.framework.exceptions;

public class InvalidPluginException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidPluginException(Class<?> cls) {
		super(cls.getName()+" is not a valid plugin.");
	}

}
