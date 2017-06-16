package org.workcraft.plugins.mpsat;

import org.workcraft.util.MessageUtils;

final class MpsatUndefinedResultHandler implements Runnable {

    private final String message;

    MpsatUndefinedResultHandler(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        MessageUtils.showInfo(message, "Verification results");
    }

}
