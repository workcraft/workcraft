package org.workcraft.plugins.petrify;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.plugins.petrify.commands.*;

@SuppressWarnings("unused")
public class PetrifyPlugin implements Plugin {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerSettings(PetrifySettings.class);

        ScriptableCommandUtils.registerCommand(NetConversionCommand.class, "convertPetriSynthesis",
                "convert the given Petri net/FSM or STG/FST 'work' into a new Petri net or STG work using net synthesis");
        ScriptableCommandUtils.registerCommand(NetErConversionCommand.class, "convertPetriSynthesisEr",
                "convert the given Petri net/FSM or STG/FST 'work' into a new Petri net or STG work using net synthesis"
                + " with a different label for each excitation region");
        ScriptableCommandUtils.registerCommand(UntoggleConversionCommand.class, "convertStgUntoggle",
                "convert the given STG 'work' into a new work where the selected (or all) transitions are untoggled");
        ScriptableCommandUtils.registerCommand(HideDummyConversionCommand.class, "convertStgHideDummy",
                "convert the given STG 'work' into a new work without dummies");
        ScriptableCommandUtils.registerCommand(HideConversionCommand.class, "convertPetriHideTransition",
                "convert the given Petri net or STG 'work' into a new Petri net or STG work hiding selected signals and dummies");
        ScriptableCommandUtils.registerCommand(HideErConversionCommand.class, "convertPetriHideErTransition",
                "convert the given Petri net or STG 'work' into a new Petri net or STG work hiding selected signals and dummies"
                + " with a different label for each excitation region");

        ScriptableCommandUtils.registerCommand(StgToFstConversionCommand.class, "convertStgToFst",
                "convert the given STG 'work' into a new FST work");
        ScriptableCommandUtils.registerCommand(StgToBinaryFstConversionCommand.class, "convertStgToBinaryFst",
                "convert the given STG 'work' into a new binary FST work");
        ScriptableCommandUtils.registerCommand(PetriToFsmConversionCommand.class, "convertPetriToFsm",
                "convert the given Petri net 'work' into a new FSM work");

        ScriptableCommandUtils.registerCommand(CscConflictResolutionCommand.class, "resolveCscConflictPetrify",
                "resolve complete state coding conflicts in STG 'work' using Petrify backend");

        ScriptableCommandUtils.registerCommand(ComplexGateSynthesisCommand.class, "synthComplexGatePetrify",
                "logic synthesis of the STG 'work' into a complex gate Circuit work using Petrify backend");
        ScriptableCommandUtils.registerCommand(GeneralisedCelementSynthesisCommand.class, "synthGeneralisedCelementPetrify",
                "synthesis of the STG 'work' into a generalised C-element Circuit work using Petrify");
        ScriptableCommandUtils.registerCommand(StandardCelementSynthesisCommand.class, "synthStandardCelementPetrify",
                "synthesis of the STG 'work' into a standard C-element Circuit work using Petrify backend");
        ScriptableCommandUtils.registerCommand(TechnologyMappingSynthesisCommand.class, "synthTechnologyMappingPetrify",
                "technology mapping of the STG 'work' into a Circuit work using Petrify backend");
    }

    @Override
    public String getDescription() {
        return "Petrify synthesis support";
    }

}
