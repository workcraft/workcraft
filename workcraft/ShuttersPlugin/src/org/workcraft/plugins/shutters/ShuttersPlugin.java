package org.workcraft.plugins.shutters;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.shutters.commands.ExtractWindowsCommand;

@SuppressWarnings("unused")
public class ShuttersPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Shutters plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerSettings(ShuttersSettings.class);
        pm.registerCommand(ExtractWindowsCommand.class);
    }

}
