package org.workcraft.framework.exceptions;

public class PluginInstantiationException extends Exception {
	private static final long serialVersionUID = 1L;

	public PluginInstantiationException(String reason) {
		super(reason);
	}
}
