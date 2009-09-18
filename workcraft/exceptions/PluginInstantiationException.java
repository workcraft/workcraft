package org.workcraft.exceptions;

public class PluginInstantiationException extends Exception {
	public PluginInstantiationException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PluginInstantiationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public PluginInstantiationException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;

	public PluginInstantiationException(String reason) {
		super(reason);
	}
}
