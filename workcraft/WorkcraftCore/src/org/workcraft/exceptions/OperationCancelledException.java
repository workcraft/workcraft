package org.workcraft.exceptions;

public class OperationCancelledException extends Exception {

    public OperationCancelledException() {
        super("Operation cancelled by the user");
    }

    public OperationCancelledException(String message) {
        super(message);
    }

}
