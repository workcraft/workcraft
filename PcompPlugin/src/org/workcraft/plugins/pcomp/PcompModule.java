package org.workcraft.plugins.pcomp;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.plugins.pcomp.commands.ParallelCompositionCommand;

public class PcompModule implements org.workcraft.Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerCommand(ParallelCompositionCommand.class);
        pm.registerSettings(PcompSettings.class);
    }

    @Override
    public String getDescription() {
        return "PComp parallel composition support";
    }

}
