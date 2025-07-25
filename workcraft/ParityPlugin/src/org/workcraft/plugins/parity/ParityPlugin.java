package org.workcraft.plugins.parity;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.parity.commands.ParitySolverCommand;
import org.workcraft.plugins.parity.commands.ParityHelpSolverCommand;
import org.workcraft.utils.ScriptableCommandUtils;

@SuppressWarnings("unused")

/**
 * ParityPlugin class that actually initiates and executes the Parity game plugin.
 */
public class ParityPlugin implements Plugin {

    /**
     * Returns the description of the particular plugin.
     * @return    "Parity Game plugin"
     */
    @Override
    public String getDescription() {
        return "Parity Game plugin";
    }

    /**
     * Initialise the Parity game plugin.
     * Outside of the model itself, init function generates the game solver
     * commands ParitySolverCommand and ParityHelpSolverCommand.
     */
    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerModelDescriptor(ParityDescriptor.class);

        ScriptableCommandUtils.registerCommand(ParitySolverCommand.class,
                "ParitySolver",
                "Analyse the distinct vertices and edges within the parity game");
        ScriptableCommandUtils.registerCommand(ParityHelpSolverCommand.class,
                "ParityHelpSolver",
                "Help for users on how to use the Parity Game Plugin");
    }

}
