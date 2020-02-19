package org.workcraft.plugins.punf;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.punf.commands.SpotVerificationCommand;

@SuppressWarnings("unused")
public class PunfPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Punf unfolding plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerSettings(PunfSettings.class);
        pm.registerCommand(SpotVerificationCommand.class);
    }

}
