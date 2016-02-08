package org.workcraft.plugins.mpsat;

import javax.swing.JOptionPane;


final class MpsatUndefinedResultHandler implements Runnable {

    private final String message;

    MpsatUndefinedResultHandler(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        String title = "Verification results";
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

}
