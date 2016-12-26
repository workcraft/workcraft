package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class InvalidComponentException extends RuntimeException {
    public InvalidComponentException(String message) {
        super(message);
    }
}
