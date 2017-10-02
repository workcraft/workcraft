package org.workcraft.plugins.petrify;

import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.Command;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.petrify.commands.PetrifyComplexGateSynthesisCommand;
import org.workcraft.plugins.petrify.commands.PetrifyCscConflictResolutionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyGeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.petrify.commands.PetrifyHideConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyHideDummyConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyNetConversionCommand;
import org.workcraft.plugins.petrify.commands.PetrifyStandardCelementSynthesisCommand;
import org.workcraft.plugins.petrify.commands.PetrifyTechnologyMappingSynthesisCommand;
import org.workcraft.plugins.petrify.commands.PetrifyUntoggleConversionCommand;

public class PetrifyModule implements Module {

    private final class PetrifyNetErConversionCommand extends PetrifyNetConversionCommand {
        @Override
        public String getDisplayName() {
            return "Net synthesis [Petrify with -er option]";
        }

        @Override
        public ArrayList<String> getArgs() {
            ArrayList<String> args = super.getArgs();
            args.add("-er");
            return args;
        }
    }

    private final class PetrifyHideErConversionCommand extends PetrifyHideConversionCommand {
        @Override
        public String getDisplayName() {
            return "Net synthesis hiding selected signals and dummies [Petrify with -er option]";
        }

        @Override
        public ArrayList<String> getArgs() {
            ArrayList<String> args = super.getArgs();
            args.add("-er");
            return args;
        }
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Settings.class, PetrifySettings.class);

        ScriptableCommandUtils.register(PetrifyNetConversionCommand.class);
        ScriptableCommandUtils.register(PetrifyUntoggleConversionCommand.class);
        ScriptableCommandUtils.register(PetrifyHideDummyConversionCommand.class);
        ScriptableCommandUtils.register(PetrifyHideConversionCommand.class);

        ScriptableCommandUtils.register(PetrifyCscConflictResolutionCommand.class, "resolveCscConflictPetrify",
                "resolve complete state coding conflicts with Petrify backend");

        ScriptableCommandUtils.register(PetrifyComplexGateSynthesisCommand.class, "synthComplexGatePetrify",
                "logic synthesis of the STG 'work' into a complex gate circuit using Petrify backend");
        ScriptableCommandUtils.register(PetrifyGeneralisedCelementSynthesisCommand.class, "synthGeneralisedCelementPetrify",
                "synthesis of the STG 'work' into a generalised C-element circuit using Petrify");
        ScriptableCommandUtils.register(PetrifyStandardCelementSynthesisCommand.class, "synthStandardCelementPetrify",
                "synthesis of the STG 'work' into a standard C-element circuit using Petrify backend");
        ScriptableCommandUtils.register(PetrifyTechnologyMappingSynthesisCommand.class, "synthTechnologyMappingPetrify",
                "technology mapping of the STG 'work' into a circuit using Petrify backend");

        pm.registerClass(Command.class, new Initialiser<Command>() {
            @Override
            public Command create() {
                return new PetrifyNetErConversionCommand();
            }
        });

        pm.registerClass(Command.class, new Initialiser<Command>() {
            @Override
            public Command create() {
                return new PetrifyHideErConversionCommand();
            }
        });
    }

    @Override
    public String getDescription() {
        return "Petrify synthesis support";
    }

}
