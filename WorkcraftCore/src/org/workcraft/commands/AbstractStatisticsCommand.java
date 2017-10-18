package org.workcraft.commands;

import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class AbstractStatisticsCommand implements ScriptableCommand<String> {

    @Override
    public final String getSection() {
        return "Statistics";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, AbstractMathModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        execute(we);
    }

    @Override
    public final String execute(WorkspaceEntry we) {
        String result = getStatistics(we);
        DialogUtils.showInfo(result, "Statistics");
        return result;
    }

    public abstract String getStatistics(WorkspaceEntry we);

}
