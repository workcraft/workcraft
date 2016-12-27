package org.workcraft.exceptions;

public class UnsupportedComponentException extends Exception {
    private static final long serialVersionUID = 1L;

    public UnsupportedComponentException() {
        super("The component that is being added is not compatible with the current model");
    }
    public UnsupportedComponentException(String why) {
        super(why);
    }
}
