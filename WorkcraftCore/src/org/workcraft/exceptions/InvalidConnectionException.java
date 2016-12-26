package org.workcraft.exceptions;

public class InvalidConnectionException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidConnectionException(String msg) {
        super(msg);
    }
}
