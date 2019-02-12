package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.plugins.mpsat.commands.MpsatComplexGateSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.MpsatGeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.MpsatStandardCelementSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.MpsatTechnologyMappingSynthesisCommand;

public class MpsatSynthesisModule implements org.workcraft.Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerSettings(MpsatSynthesisSettings.class);

        ScriptableCommandUtils.register(MpsatComplexGateSynthesisCommand.class, "synthComplexGateMpsat",
                "logic synthesis of the STG 'work' into a complex gate Circuit work using MPSat backend");
        ScriptableCommandUtils.register(MpsatGeneralisedCelementSynthesisCommand.class, "synthGeneralisedCelementMpsat",
                "synthesis of the STG 'work' into a generalised C-element Circuit work using MPSat backend");
        ScriptableCommandUtils.register(MpsatStandardCelementSynthesisCommand.class, "synthStandardCelementMpsat",
                "synthesis of the STG 'work' into a standard C-element Circuit work using MPSat backend");
        ScriptableCommandUtils.register(MpsatTechnologyMappingSynthesisCommand.class, "synthTechnologyMappingMpsat",
                "technology mapping of the STG 'work' into a Circuit work using MPSat backend");
    }

    @Override
    public String getDescription() {
        return "MPSat synthesis support";
    }

}
