package org.workcraft.exceptions;

@Deprecated // To warn users that they should implement the feature
public class NotImplementedException extends RuntimeException {

    public NotImplementedException() {
        super("The feature is not implemented yet");
    }

    public NotImplementedException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -6828334836877473788L;

}
