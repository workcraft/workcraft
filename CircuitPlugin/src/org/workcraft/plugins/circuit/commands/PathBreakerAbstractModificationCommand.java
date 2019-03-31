package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.gui.Toolbox;
import org.workcraft.plugins.circuit.tools.CycleAnalyserTool;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class PathBreakerAbstractModificationCommand extends AbstractModificationCommand {

    @Override
    public Void execute(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            final Toolbox toolbox = framework.getMainWindow().getEditor(we).getToolBox();
            toolbox.selectTool(toolbox.getToolInstance(CycleAnalyserTool.class));
        }
        return null;
    }

}
