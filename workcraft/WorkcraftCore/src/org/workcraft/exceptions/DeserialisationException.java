package org.workcraft.exceptions;

@SuppressWarnings("serial")
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
