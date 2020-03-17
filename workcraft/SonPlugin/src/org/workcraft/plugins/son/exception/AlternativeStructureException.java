package org.workcraft.plugins.son.exception;

public class AlternativeStructureException extends Exception {

    private static final long serialVersionUID = 1L;

    public AlternativeStructureException(String msg) {
        super("Model has alternative behaviours: " + msg);
    }
}
