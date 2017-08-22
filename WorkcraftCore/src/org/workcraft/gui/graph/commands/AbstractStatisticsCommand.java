package org.workcraft.gui.graph.commands;

import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class AbstractStatisticsCommand implements ScriptableCommand {

    @Override
    public final String getSection() {
        return "Statistics";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, AbstractMathModel.class);
    }

    @Override
    public final WorkspaceEntry execute(WorkspaceEntry we) {
        DialogUtils.showInfo(getStatistics(we), "Statistics");
        return we;
    }

    @Override
    public final void run(WorkspaceEntry we) {
        execute(we);
    }

    public abstract String getStatistics(WorkspaceEntry we);
}
