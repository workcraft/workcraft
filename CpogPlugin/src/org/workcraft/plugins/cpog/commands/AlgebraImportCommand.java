package org.workcraft.plugins.cpog.commands;

import java.io.File;

import javax.swing.JFileChooser;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class AlgebraImportCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }

    @Override
    public String getSection() {
        return "! Algebra";
    }

    @Override
    public String getDisplayName() {
        return "Import CPOG expressions from file";
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            LogUtils.logError("Tool '" + getClass().getSimpleName() + "' only works in GUI mode.");
        } else {
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

}
