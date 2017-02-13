package org.workcraft.plugins.fst.commands;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.fst.tasks.StgToFstConversionResultHandler;
import org.workcraft.plugins.fst.tasks.WriteSgConversionTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class StgToFstConversionCommand extends AbstractConversionCommand {

    public boolean isBinary() {
        return false;
    }

    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public String getDisplayName() {
        if (isBinary()) {
            return "Finite State Transducer (binary-encoded) [Petrify]";
        } else {
            return "Finite State Transducer (basic) [Petrify]";
        }
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        if (Hierarchy.isHierarchical(we.getModelEntry())) {
            final MainWindow mainWindow = framework.getMainWindow();
            JOptionPane.showMessageDialog(mainWindow,
                    "Finite State Transducer cannot be derived from a hierarchical Signal Transition Graph.",
                    "Conversion error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        final TaskManager taskManager = framework.getTaskManager();
        final WriteSgConversionTask task = new WriteSgConversionTask(we, isBinary());
        final StgToFstConversionResultHandler monitor = new StgToFstConversionResultHandler(task);
        taskManager.execute(task, "Building state graph", monitor);
        return monitor.getResult();
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

}
