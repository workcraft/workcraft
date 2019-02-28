package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.utils.DialogUtils;

final class MpsatUndefinedResultHandler implements Runnable {

    private final String message;

    MpsatUndefinedResultHandler(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        DialogUtils.showInfo(message, "Verification results");
    }

}
