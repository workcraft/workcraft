package org.workcraft.exceptions;

public class ModelCheckingFailedException extends Exception {
    private static final long serialVersionUID = 1L;

    public ModelCheckingFailedException(String message) {
        super(message);
    }
}
