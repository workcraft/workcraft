package org.workcraft.plugins.parity.commands;

import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

/**
 * Game Solver Command that sits in the 'Tools' section of the ribbon.
 * Contains the ParityHelpSolverCommand, and ParitySolverCommand within the
 * 'Game Solver' menu that this class creates.
 */
public abstract class AbstractGameSolverCommand implements ScriptableCommand<String> {

    /**
     * Returns the title of the menu to be created.
     * @return    Title of the menu to be displayed. 'Game Solver'
     */
    @Override
    public final Section getSection() {
        return new Section("Game Solver");
    }

    /**
     * Predicate function to check if the WorkspaceEntry is applicable in
     * regards to the AbstractMathModel.
     * @param we    WorkspaceEntry to check for validity
     * @return      True if the Workspace is applicable to the AbstractMathModel
     */
    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, AbstractMathModel.class);
    }

    /**
     * Run the WorkspaceEntry.
     * @param we    WorkspaceEntry to be ran
     */
    @Override
    public final void run(WorkspaceEntry we) {
        execute(we);
    }

    /**
     * Helper function to run. This gathers the game solver, shows info based on
     * gamesolver, and returns whatever result was gathered. This will be what
     * was generated when the corresponding button (instructions or the solver)
     * were clicked.
     * @param we    Current WorkspaceEntry
     * @return      Text output from the corresponding submenu box being clicked
     */
    @Override
    public final String execute(WorkspaceEntry we) {
        String result = getGameSolver(we);
        DialogUtils.showInfo(result, "Game Solver");
        return result;
    }

    public abstract String getGameSolver(WorkspaceEntry we);

}
