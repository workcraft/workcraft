package org.workcraft.plugins.atacs;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.atacs.commands.AtacsComplexGateSynthesisCommand;
import org.workcraft.plugins.atacs.commands.AtacsGeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.atacs.commands.AtacsStandardCelementSynthesisCommand;

public class AtacsModule implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Settings.class, AtacsSettings.class);

        ScriptableCommandUtils.register(AtacsComplexGateSynthesisCommand.class, "synthComplexGateAtacs",
                "logic synthesis of the STG 'work' into a complex gate Circuit work using ATACS backend");
        ScriptableCommandUtils.register(AtacsGeneralisedCelementSynthesisCommand.class, "synthGeneralisedCelementAtacs",
                "synthesis of the STG 'work' into a generalised C-element Circuit work using ATACS");
        ScriptableCommandUtils.register(AtacsStandardCelementSynthesisCommand.class, "synthStandardCelementAtacs",
                "synthesis of the STG 'work' into a standard C-element Circuit work using ATACS backend");
    }

    @Override
    public String getDescription() {
        return "ATACS synthesis support";
    }

}
