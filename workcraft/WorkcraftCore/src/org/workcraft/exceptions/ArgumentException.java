package org.workcraft.exceptions;

public class ArgumentException extends RuntimeException {

    public ArgumentException() {
        super();
    }

    public ArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArgumentException(String message) {
        super(message);
    }

    public ArgumentException(Throwable cause) {
        super(cause);
    }

}
