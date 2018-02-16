package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class LayoutFailedException extends Exception {

    public LayoutFailedException() {
        super();
    }

    public LayoutFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LayoutFailedException(String message) {
        super(message);
    }

    public LayoutFailedException(Throwable cause) {
        super(cause);
    }

}
