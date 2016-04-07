package org.workcraft.plugins.son.exception;

public class SyncCycleException extends Exception {

    private static final long serialVersionUID = 1L;

    public SyncCycleException() {
        super("Model involves synchronous cycle, cannot run entire SON estimation");
    }
}
