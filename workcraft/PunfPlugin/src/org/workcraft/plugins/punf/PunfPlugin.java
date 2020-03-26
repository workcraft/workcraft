package org.workcraft.plugins.punf;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.punf.commands.SpotAssertionVerificationCommand;
import org.workcraft.utils.ScriptableCommandUtils;

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
        ScriptableCommandUtils.registerDataCommand(SpotAssertionVerificationCommand.class, "checkStgSpotAssertion",
                "check STG 'work' for SPOT assertion 'data'");
    }

}
