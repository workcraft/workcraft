package org.workcraft.plugins.mpsat_temporal;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.mpsat_temporal.commands.SpotAssertionVerificationCommand;
import org.workcraft.utils.ScriptableCommandUtils;

@SuppressWarnings("unused")
public class MpsatTemporalPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "MPSat temporal plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerSettings(MpsatTemporalSettings.class);

        ScriptableCommandUtils.registerDataCommand(SpotAssertionVerificationCommand.class, "checkStgSpotAssertion",
                "check the STG 'work' for SPOT assertion 'data'");
    }

}
