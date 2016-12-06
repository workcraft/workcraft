package org.workcraft.plugins.cpog.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.cpog.PetriToCpogSettings;
import org.workcraft.plugins.cpog.gui.PetriToCpogDialog;
import org.workcraft.plugins.cpog.tasks.PetriToCpogHandler;
import org.workcraft.plugins.cpog.tasks.PetriToCpogTask;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriToCpogTool extends ConversionTool {

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, PetriNet.class);
    }

    @Override
    public String getDisplayName() {
        return "Conditional Partial Order Graph [Untanglings]";
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();

        PetriToCpogSettings settings = new PetriToCpogSettings();
        PetriToCpogDialog dialog = new PetriToCpogDialog(mainWindow, settings, we);
        GUI.centerToParent(dialog, mainWindow);
        dialog.setVisible(true);
        if (dialog.getModalResult() == 1) {
            // Instantiate Solver
            PetriToCpogTask task = new PetriToCpogTask(we, settings);
            // Instantiate object for handling solution
            PetriToCpogHandler result = new PetriToCpogHandler(task);
            //Run both
            final TaskManager taskManager = framework.getTaskManager();
            taskManager.queue(task, "Converting Petri net into CPOG...", result);
        }
        return we;
    }

}
