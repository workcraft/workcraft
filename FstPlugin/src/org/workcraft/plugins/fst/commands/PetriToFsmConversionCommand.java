package org.workcraft.plugins.fst.commands;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.fst.tasks.PetriToFsmConversionResultHandler;
import org.workcraft.plugins.fst.tasks.WriteSgConversionTask;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetriToFsmConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Finite State Machine [Petrify]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNet.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        if (Hierarchy.isHierarchical(we.getModelEntry())) {
            final MainWindow mainWindow = framework.getMainWindow();
            JOptionPane.showMessageDialog(mainWindow,
                    "Finite State Machine cannot be derived from a hierarchical Petri Net.",
                    "Conversion error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        final TaskManager taskManager = framework.getTaskManager();
        final WriteSgConversionTask task = new WriteSgConversionTask(we, false);
        final PetriToFsmConversionResultHandler monitor = new PetriToFsmConversionResultHandler(task);
        taskManager.execute(task, "Building state graph", monitor);
        return monitor.getResult();
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

}
