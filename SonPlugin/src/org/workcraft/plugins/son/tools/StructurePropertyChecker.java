package org.workcraft.plugins.son.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.son.BlockConnector;
import org.workcraft.plugins.son.OutputRedirect;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.gui.StructureVerifyDialog;
import org.workcraft.plugins.son.tasks.SONMainTask;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class StructurePropertyChecker implements Tool {

    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, SON.class);
    }

    public String getSection() {
        return "Verification";
    }

    public String getDisplayName() {
        return "Structural properties...";
    }

    public void run(WorkspaceEntry we) {

        VisualSON visualNet = (VisualSON) we.getModelEntry().getVisualModel();
        SON net = (SON) we.getModelEntry().getMathModel();

        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();

        net.refreshAllColor();
        StructureVerifyDialog dialog = new StructureVerifyDialog(mainWindow, we);
        GUI.centerToParent(dialog, mainWindow);
        dialog.setVisible(true);

        if (dialog.getRun() == 1) {
            OutputRedirect.redirect(30, 40, "Structure Verification Result");
            BlockConnector.blockBoundingConnector(visualNet);
            SONMainTask sonTask = new SONMainTask(dialog.getSettings(), we);
            framework.getTaskManager().queue(sonTask, "Verification");
        }
        net.refreshAllColor();
    }
}
