package org.workcraft.plugins.mpsat_synthesis;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.mpsat_synthesis.commands.*;
import org.workcraft.utils.ScriptableCommandUtils;

@SuppressWarnings("unused")
public class MpsatSynthesisPlugin implements Plugin {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerSettings(MpsatSynthesisSettings.class);

        ScriptableCommandUtils.register(CscConflictResolutionCommand.class, "resolveCscConflictMpsat",
                "resolve complete state coding conflicts with MPSat backend");

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
