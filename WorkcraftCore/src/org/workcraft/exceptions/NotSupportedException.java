package org.workcraft.exceptions;

public class NotSupportedException extends RuntimeException {

    public NotSupportedException(String message) {
        super(message);
    }

    public NotSupportedException() {
        super("The feature is not supported");
    }

    private static final long serialVersionUID = -6828334836877473788L;

}
