package org.workcraft.plugins.cpog.commands;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.utils.ScriptableCommandUtils;
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
        if (!framework.isInGuiMode()) {
            ScriptableCommandUtils.showErrorRequiresGui(getClass());
            return;
        }
        we.captureMemento();
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            final MainWindow mainWindow = framework.getMainWindow();
            final Toolbox toolbox = mainWindow.getCurrentToolbox();
            final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);
            File file = chooser.getSelectedFile();
            if (tool.insertCpogFromFile(file)) {
                we.saveMemento();
            } else {
                we.cancelMemento();
            }
        }
    }

}
