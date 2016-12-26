package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class ModelInstantiationException extends Exception {

    public ModelInstantiationException() {
        super();
    }

    public ModelInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelInstantiationException(String message) {
        super(message);
    }

    public ModelInstantiationException(Throwable cause) {
        super(cause);
    }

}
