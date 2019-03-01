package org.workcraft.plugins.atacs;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.plugins.atacs.commands.AtacsComplexGateSynthesisCommand;
import org.workcraft.plugins.atacs.commands.AtacsGeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.atacs.commands.AtacsStandardCelementSynthesisCommand;

@SuppressWarnings("unused")
public class AtacsPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "ATACS synthesis plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerSettings(AtacsSettings.class);

        ScriptableCommandUtils.register(AtacsComplexGateSynthesisCommand.class, "synthComplexGateAtacs",
                "logic synthesis of the STG 'work' into a complex gate Circuit work using ATACS backend");
        ScriptableCommandUtils.register(AtacsGeneralisedCelementSynthesisCommand.class, "synthGeneralisedCelementAtacs",
                "synthesis of the STG 'work' into a generalised C-element Circuit work using ATACS");
        ScriptableCommandUtils.register(AtacsStandardCelementSynthesisCommand.class, "synthStandardCelementAtacs",
                "synthesis of the STG 'work' into a standard C-element Circuit work using ATACS backend");
    }

}
