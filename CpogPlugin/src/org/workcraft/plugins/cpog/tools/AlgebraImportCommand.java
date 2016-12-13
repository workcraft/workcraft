package org.workcraft.plugins.cpog.tools;

import java.io.File;

import javax.swing.JFileChooser;

import org.workcraft.Command;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class AlgebraImportCommand implements Command {

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, VisualCpog.class);
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
    public ModelEntry run(ModelEntry me) {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            LogUtils.logErrorLine("Tool '" + getClass().getSimpleName() + "' only works in GUI mode.");
        } else {
            final WorkspaceEntry we = framework.getWorkspaceEntry(me);
            we.captureMemento();
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                final MainWindow mainWindow = framework.getMainWindow();
                final ToolboxPanel toolbox = mainWindow.getCurrentToolbox();
                final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);
                File file = chooser.getSelectedFile();
                if (tool.insertCpogFromFile(file)) {
                    we.saveMemento();
                } else {
                    we.cancelMemento();
                }
            }
        }
        return me;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        run(we.getModelEntry());
        return we;
    }

}
