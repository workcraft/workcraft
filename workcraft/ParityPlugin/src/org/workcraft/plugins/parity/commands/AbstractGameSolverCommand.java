package org.workcraft.plugins.parity.commands;

import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

/**
 * Game Solver Command that sits in the 'Tools' section of the ribbon.
 */
public abstract class AbstractGameSolverCommand implements ScriptableCommand<String> {

    private static final String SECTION_TITLE = "Game Solver";

    @Override
    public final String getSection() {
        return SECTION_TITLE;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, AbstractMathModel.class);
    }

    @Override
    public final void run(WorkspaceEntry we) {
        execute(we);
    }

    @Override
    public final String execute(WorkspaceEntry we) {
        String result = getGameSolver(we);
        DialogUtils.showInfo(result, "Game Solver");
        return result;
    }

    public abstract String getGameSolver(WorkspaceEntry we);

}