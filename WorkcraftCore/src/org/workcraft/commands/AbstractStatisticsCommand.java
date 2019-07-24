package org.workcraft.commands;

import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractStatisticsCommand implements ScriptableCommand<String> {

    public static final String SECTION_TITLE = "Statistics";

    @Override
    public final String getSection() {
        return SECTION_TITLE;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, AbstractMathModel.class);
    }

    @Override
    public final String execute(WorkspaceEntry we) {
        String result = getStatistics(we);
        DialogUtils.showInfo(result, "Statistics");
        return result;
    }

    public abstract String getStatistics(WorkspaceEntry we);

}
