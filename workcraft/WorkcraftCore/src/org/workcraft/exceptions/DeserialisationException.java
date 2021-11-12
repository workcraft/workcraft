package org.workcraft.exceptions;

public class DeserialisationException extends Exception {

    public DeserialisationException() {
    }

    public DeserialisationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeserialisationException(String message) {
        super(message);
    }

    public DeserialisationException(Throwable cause) {
        super(cause);
    }

}
