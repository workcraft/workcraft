package org.workcraft.plugins.petrify;

import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.petrify.tools.CscConflictResolutionCommand;
import org.workcraft.plugins.petrify.tools.HideDummyConversionCommand;
import org.workcraft.plugins.petrify.tools.NetConversionCommand;
import org.workcraft.plugins.petrify.tools.HideConversionCommand;
import org.workcraft.plugins.petrify.tools.ComplexGateSynthesisCommand;
import org.workcraft.plugins.petrify.tools.GeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.petrify.tools.StandardCelementSynthesisCommand;
import org.workcraft.plugins.petrify.tools.TechnologyMappingSynthesisCommand;
import org.workcraft.plugins.petrify.tools.UntoggleConversionCommand;

public class PetrifyModule implements Module {

    private final class PetrifyNetErConversionCommand extends NetConversionCommand {
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

    private final class PetrifyHideErConversionCommand extends HideConversionCommand {
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

        pm.registerClass(Settings.class, PetrifyUtilitySettings.class);

        pm.registerClass(Command.class, UntoggleConversionCommand.class);
        pm.registerClass(Command.class, CscConflictResolutionCommand.class);
        pm.registerClass(Command.class, ComplexGateSynthesisCommand.class);
        pm.registerClass(Command.class, GeneralisedCelementSynthesisCommand.class);
        pm.registerClass(Command.class, StandardCelementSynthesisCommand.class);
        pm.registerClass(Command.class, TechnologyMappingSynthesisCommand.class);
        pm.registerClass(Command.class, HideDummyConversionCommand.class);
        pm.registerClass(Command.class, NetConversionCommand.class);
        pm.registerClass(Command.class, HideConversionCommand.class);

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
