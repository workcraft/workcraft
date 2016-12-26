package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class VisualComponentCreationException extends Exception {

    public VisualComponentCreationException() {
        super();
    }

    public VisualComponentCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public VisualComponentCreationException(String message) {
        super(message);
    }

    public VisualComponentCreationException(Throwable cause) {
        super(cause);
    }

}
