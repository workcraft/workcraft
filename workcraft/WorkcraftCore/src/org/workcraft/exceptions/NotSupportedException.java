package org.workcraft.exceptions;

public class NotSupportedException extends RuntimeException {

    public NotSupportedException() {
        super("The feature is not supported");
    }

    public NotSupportedException(String message) {
        super(message);
    }

}
