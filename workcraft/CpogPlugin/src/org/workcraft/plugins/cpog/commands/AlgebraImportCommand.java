package org.workcraft.plugins.cpog.commands;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.File;

public class AlgebraImportCommand extends AbstractAlgebraCommand {

    @Override
    public String getDisplayName() {
        return "Import CPOG expressions from file";
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        we.captureMemento();
        JFileChooser fc = new JFileChooser();
        if (DialogUtils.showFileOpener(fc)) {
            final MainWindow mainWindow = framework.getMainWindow();
            final Toolbox toolbox = mainWindow.getCurrentToolbox();
            final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);
            File file = fc.getSelectedFile();
            if (tool.insertCpogFromFile(file)) {
                we.saveMemento();
            } else {
                we.cancelMemento();
            }
        }
    }

}
