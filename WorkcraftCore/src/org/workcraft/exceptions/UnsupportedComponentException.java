package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class UnsupportedComponentException extends Exception {

    public UnsupportedComponentException() {
        super("The component that is being added is not compatible with the current model");
    }

    public UnsupportedComponentException(String message) {
        super(message);
    }

}
