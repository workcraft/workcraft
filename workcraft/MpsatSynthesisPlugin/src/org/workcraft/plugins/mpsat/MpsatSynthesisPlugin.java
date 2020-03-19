package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.plugins.mpsat.commands.ComplexGateSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.GeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.StandardCelementSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.TechnologyMappingSynthesisCommand;

@SuppressWarnings("unused")
public class MpsatSynthesisPlugin implements Plugin {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerSettings(MpsatSynthesisSettings.class);

        ScriptableCommandUtils.register(ComplexGateSynthesisCommand.class, "synthComplexGateMpsat",
                "logic synthesis of the STG 'work' into a complex gate Circuit work using MPSat backend");
        ScriptableCommandUtils.register(GeneralisedCelementSynthesisCommand.class, "synthGeneralisedCelementMpsat",
                "synthesis of the STG 'work' into a generalised C-element Circuit work using MPSat backend");
        ScriptableCommandUtils.register(StandardCelementSynthesisCommand.class, "synthStandardCelementMpsat",
                "synthesis of the STG 'work' into a standard C-element Circuit work using MPSat backend");
        ScriptableCommandUtils.register(TechnologyMappingSynthesisCommand.class, "synthTechnologyMappingMpsat",
                "technology mapping of the STG 'work' into a Circuit work using MPSat backend");
    }

    @Override
    public String getDescription() {
        return "MPSat synthesis support";
    }

}
