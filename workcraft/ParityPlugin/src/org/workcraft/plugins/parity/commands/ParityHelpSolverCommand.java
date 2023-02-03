package org.workcraft.plugins.parity.commands;

import org.workcraft.plugins.parity.Parity;
import org.workcraft.commands.AbstractGameSolverCommand;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

/**
 * Class involving the help text for the game solver. This informs the user on
 * how to use the parity game solver.
 */
public class ParityHelpSolverCommand extends AbstractGameSolverCommand {

    /**
     * Get the display name of the text box to be clicked to view the game
     * solver help
     */
    @Override
    public String getDisplayName() {
        return "Game Solver Instructions";
    }

    /**
     * Ensure the WorkspaceEntry can be worked on.
     */
    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Parity.class);
    }

    /**
     * Generate the game solver help here.
     * @param we    Current WorkspaceEntry
     * @return      Instructions on how to use the Parity game solver
     */
    @Override
    public String getGameSolver(WorkspaceEntry we) {
        return "Welcome to the parity game solver!\n\n"
            + "This parity game solver solves max-parity style games\n"
            + "Player 0 vertices are represented as circles\n"
            + "Player 1 vertices are represented as squares\n\n"
            + "Player 0 wins if the largest priority is even\n"
            + "Player 1 wins if the largest priority is odd\n"
            + "Winning regions (both vertices and possible winning strategies)\n"
            + "are coloured blue if Player 0 wins, and red if player 1 wins\n\n"
            + "INSTRUCTIONS:\n"
            + "1) Place down any amount of player 0 and player 1 vertices\n"
            + "    by clicking the circle and square icons in the top ribbon\n"
            + "2) Add edges using the edge/Connect icon in the top ribbon.\n"
            + "    Ensure every vertex has at least 1 outgoing edge\n"
            + "3) Edit vertex priorities by clicking the pointer icon in the\n"
            + "    top ribbon, clicking one of the vertices, and editing\n"
            + "    the priority value in the 'Priority' right hand text box.\n"
            + "    Vertex priorities cannot be non-negative\n"
            + "4) Click the 'Solve Game' option in the Tools -> Game Solver\n"
            + "    menu to solve the game\n\n"
            + "This parity game solver uses Oink to solve the games,\n"
            + "developed by Tom van Dijk.\n\n"
            + "Oink was not designed to be built on Windows natively.\n"
            + "Plugin will run on MacOS, Linux distros,\n"
            + "or Windows using WSL/Linux VM\n\n"
            + "The URL to the Oink Github can be found below:\n"
            + "https://github.com/trolando/oink";
    }
}