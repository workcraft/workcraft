package org.workcraft.exceptions;

public class SerialisationException extends Exception {

    public SerialisationException() {
        super();
    }

    public SerialisationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerialisationException(String message) {
        super(message);
    }

    public SerialisationException(Throwable cause) {
        super(cause);
    }

}
