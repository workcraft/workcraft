package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class InvalidPluginException extends Exception {

    public InvalidPluginException(Class<?> cls) {
        super(cls.getName() + " is not a valid plugin.");
    }

}
