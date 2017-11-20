package org.workcraft.plugins.petrify;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.petrify.commands.PetrifyPetriToFsmConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyComplexGateSynthesisCommand;
import org.workcraft.plugins.petrify.commands.PetrifyCscConflictResolutionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyGeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.petrify.commands.PetrifyHideConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyHideDummyConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyHideErConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyNetConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyNetErConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyStandardCelementSynthesisCommand;
import org.workcraft.plugins.petrify.commands.PetrifyTechnologyMappingSynthesisCommand;
import org.workcraft.plugins.petrify.commands.PetrifyUntoggleConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyStgToBinaryFstConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyStgToFstConversionCommand;

public class PetrifyModule implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Settings.class, PetrifySettings.class);

        ScriptableCommandUtils.register(PetrifyNetConversionCommand.class, "convertPetriSynthesis",
                "convert the given Petri net/FSM or STG/FST 'work' into a new Petri net or STG work using net synthesis");
        ScriptableCommandUtils.register(PetrifyNetErConversionCommand.class, "convertPetriSynthesisEr",
                "convert the given Petri net/FSM or STG/FST 'work' into a new Petri net or STG work using net synthesis"
                + " with a different label for each excitation region");
        ScriptableCommandUtils.register(PetrifyUntoggleConversionCommand.class, "convertStgUntoggle",
                "convert the given STG 'work' into a new work where the selected (or all) transitions are untoggled");
        ScriptableCommandUtils.register(PetrifyHideDummyConversionCommand.class, "convertStgHideDummy",
                "convert the given STG 'work' into a new work without dummies");
        ScriptableCommandUtils.register(PetrifyHideConversionCommand.class, "convertPetriHideTransition",
                "convert the given Petri net or STG 'work' into a new Petri net or STG work hiding selected signals and dummies");
        ScriptableCommandUtils.register(PetrifyHideErConversionCommand.class, "convertPetriHideErTransition",
                "convert the given Petri net or STG 'work' into a new Petri net or STG work hiding selected signals and dummies"
                + " with a different label for each excitation region");

        ScriptableCommandUtils.register(PetrifyStgToFstConversionCommand.class, "convertStgToFst",
                "convert the given STG 'work' into a new FST work");
        ScriptableCommandUtils.register(PetrifyStgToBinaryFstConversionCommand.class, "convertStgToBinaryFst",
                "convert the given STG 'work' into a new binary FST work");
        ScriptableCommandUtils.register(PetrifyPetriToFsmConversionCommand.class, "convertPetriToFsm",
                "convert the given Petri net 'work' into a new FSM work");

        ScriptableCommandUtils.register(PetrifyCscConflictResolutionCommand.class, "resolveCscConflictPetrify",
                "resolve complete state coding conflicts with Petrify backend");

        ScriptableCommandUtils.register(PetrifyComplexGateSynthesisCommand.class, "synthComplexGatePetrify",
                "logic synthesis of the STG 'work' into a complex gate Circuit work using Petrify backend");
        ScriptableCommandUtils.register(PetrifyGeneralisedCelementSynthesisCommand.class, "synthGeneralisedCelementPetrify",
                "synthesis of the STG 'work' into a generalised C-element Circuit work using Petrify");
        ScriptableCommandUtils.register(PetrifyStandardCelementSynthesisCommand.class, "synthStandardCelementPetrify",
                "synthesis of the STG 'work' into a standard C-element Circuit work using Petrify backend");
        ScriptableCommandUtils.register(PetrifyTechnologyMappingSynthesisCommand.class, "synthTechnologyMappingPetrify",
                "technology mapping of the STG 'work' into a Circuit work using Petrify backend");
    }

    @Override
    public String getDescription() {
        return "Petrify synthesis support";
    }

}
