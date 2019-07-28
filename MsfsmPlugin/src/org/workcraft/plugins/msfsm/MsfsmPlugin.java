package org.workcraft.plugins.msfsm;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.msfsm.commands.SyncFsmConversionCommand;
import org.workcraft.utils.ScriptableCommandUtils;

@SuppressWarnings("unused")
public class MsfsmPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "MSFSM conversion plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerSettings(MsfsmSettings.class);

        ScriptableCommandUtils.register(SyncFsmConversionCommand.class, "convertSyncFsmMsfsm",
                "convert Petri net 'work' into multiple synchronised FSMs using MSFSM backend");
    }

}
