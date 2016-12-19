package org.workcraft.plugins.son.commands;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.son.BlockConnector;
import org.workcraft.plugins.son.OutputRedirect;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.gui.StructureVerifyDialog;
import org.workcraft.plugins.son.tasks.SONMainTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class StructurePropertyCheckerCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public String getSection() {
        return "Verification";
    }

    @Override
    public String getDisplayName() {
        return "Structural properties...";
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        VisualSON visualNet = (VisualSON) we.getModelEntry().getVisualModel();
        SON net = WorkspaceUtils.getAs(we, SON.class);

        net.refreshAllColor();
        StructureVerifyDialog dialog = new StructureVerifyDialog(mainWindow, we);
        GUI.centerToParent(dialog, mainWindow);
        dialog.setVisible(true);

        if (dialog.getRun() == 1) {
            OutputRedirect.redirect(30, 40, "Structure Verification Result");
            BlockConnector.blockBoundingConnector(visualNet);
            SONMainTask sonTask = new SONMainTask(dialog.getSettings(), we);
            final TaskManager taskManager = framework.getTaskManager();
            taskManager.queue(sonTask, "Verification");
        }
        net.refreshAllColor();
    }

}
