package org.workcraft.exceptions;

@SuppressWarnings("serial")
@Deprecated // To warn users that they should implement the feature
public class NotImplementedException extends RuntimeException {

    public NotImplementedException() {
        super("The feature is not implemented yet");
    }

    public NotImplementedException(String message) {
        super(message);
    }

}
