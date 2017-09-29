package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.mpsat.commands.MpsatComplexGateSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.MpsatGeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.MpsatStandardCelementSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.MpsatTechnologyMappingSynthesisCommand;

public class MpsatSynthesisModule implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerClass(Settings.class, MpsatSynthesisSettings.class);

        ScriptableCommandUtils.register(MpsatComplexGateSynthesisCommand.class, "synthComplexGateMpsat",
                "logic synthesis of the STG into a complex gate circuit using MPSat backend");
        ScriptableCommandUtils.register(MpsatGeneralisedCelementSynthesisCommand.class, "synthGeneralisedCelementMpsat",
                "synthesis of the STG into a generalised C-element circuit using MPSat");
        ScriptableCommandUtils.register(MpsatStandardCelementSynthesisCommand.class, "synthStandardCelementMpsat",
                "synthesis of the STG into a standard C-element circuit using MPSat backend");
        ScriptableCommandUtils.register(MpsatTechnologyMappingSynthesisCommand.class, "synthTechnologyMappingMpsat",
                "technology mapping of the STG into a circuit using MPSat backend");
    }

    @Override
    public String getDescription() {
        return "MPSat synthesis support";
    }

}
