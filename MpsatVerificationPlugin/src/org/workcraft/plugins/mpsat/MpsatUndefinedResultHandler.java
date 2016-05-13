package org.workcraft.plugins.mpsat;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;

final class MpsatUndefinedResultHandler implements Runnable {

    private final String message;

    MpsatUndefinedResultHandler(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        String title = "Verification results";
        JOptionPane.showMessageDialog(mainWindow, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

}
