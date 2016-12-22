package org.workcraft.plugins.pcomp;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.pcomp.commands.ParallelCompositionCommand;

public class PcompModule implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Command.class, ParallelCompositionCommand.class);
        pm.registerClass(Settings.class, PcompUtilitySettings.class);
    }

    @Override
    public String getDescription() {
        return "PComp parallel composition support";
    }
}
