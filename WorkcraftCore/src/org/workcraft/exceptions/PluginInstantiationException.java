package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class PluginInstantiationException extends Exception {

    public PluginInstantiationException() {
        super();
    }

    public PluginInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginInstantiationException(String message) {
        super(message);
    }

    public PluginInstantiationException(Throwable cause) {
        super(cause);
    }

}
