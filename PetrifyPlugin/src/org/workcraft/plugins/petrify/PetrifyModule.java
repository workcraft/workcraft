package org.workcraft.plugins.petrify;

import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.Command;
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

        pm.registerClass(Command.class, PetrifyUntoggleConversionCommand.class);
        pm.registerClass(Command.class, PetrifyCscConflictResolutionCommand.class);
        pm.registerClass(Command.class, PetrifyComplexGateSynthesisCommand.class);
        pm.registerClass(Command.class, PetrifyGeneralisedCelementSynthesisCommand.class);
        pm.registerClass(Command.class, PetrifyStandardCelementSynthesisCommand.class);
        pm.registerClass(Command.class, PetrifyTechnologyMappingSynthesisCommand.class);
        pm.registerClass(Command.class, PetrifyHideDummyConversionCommand.class);
        pm.registerClass(Command.class, PetrifyNetConversionCommand.class);
        pm.registerClass(Command.class, PetrifyHideConversionCommand.class);

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
