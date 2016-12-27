package org.workcraft.exceptions;

public class PluginInstantiationException extends Exception {
    private static final long serialVersionUID = 1L;

    public PluginInstantiationException() {
        super();
    }

    public PluginInstantiationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public PluginInstantiationException(Throwable arg0) {
        super(arg0);
    }


    public PluginInstantiationException(String reason) {
        super(reason);
    }
}
