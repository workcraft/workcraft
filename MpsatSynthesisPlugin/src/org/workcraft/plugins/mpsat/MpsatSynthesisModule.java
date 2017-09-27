package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.Command;
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

        pm.registerClass(Command.class, MpsatComplexGateSynthesisCommand.class);
        pm.registerClass(Command.class, MpsatGeneralisedCelementSynthesisCommand.class);
        pm.registerClass(Command.class, MpsatStandardCelementSynthesisCommand.class);
        pm.registerClass(Command.class, MpsatTechnologyMappingSynthesisCommand.class);
        pm.registerClass(Settings.class, MpsatSynthesisSettings.class);
    }

    @Override
    public String getDescription() {
        return "MPSat synthesis support";
    }
}
