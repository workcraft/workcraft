package org.workcraft.plugins.son.exception;

public class TimeEstimationException extends Exception {

    private static final long serialVersionUID = 1L;

    public TimeEstimationException(String msg) {
        super("Fail to set estimated value: " + msg);
    }
}
