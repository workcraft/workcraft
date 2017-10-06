package org.workcraft.plugins.workspace.handlers;

import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.util.Import;
import org.workcraft.workspace.FileHandler;

public class WorkcraftOpen implements FileHandler {

    public boolean accept(File f) {
        final Framework framework = Framework.getInstance();
        if (Import.chooseBestImporter(framework.getPluginManager(), f) != null) {
            return true;
        } else {
            return false;
        }
    }

    public void execute(File file) {
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        try {
            framework.loadWork(file);
        } catch (DeserialisationException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainWindow, e.getMessage(), "Import error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public String getDisplayName() {
        return "Open in Workcraft";
    }
}
