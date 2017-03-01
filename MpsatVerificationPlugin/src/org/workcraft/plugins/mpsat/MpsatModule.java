package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.mpsat.commands.MpsatAssertionVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatCombinedVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatConformationVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatConsistencyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatCscConflictResolutionCommand;
import org.workcraft.plugins.mpsat.commands.MpsatCscVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatDeadlockVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatDiInterfaceVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatInputPropernessVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatNormalcyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatOutputPersistencyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatPropertyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatUscVerificationCommand;

public class MpsatModule implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Command.class, MpsatCscConflictResolutionCommand.class);
        pm.registerClass(Command.class, MpsatConsistencyVerificationCommand.class);
        pm.registerClass(Command.class, MpsatDeadlockVerificationCommand.class);
        pm.registerClass(Command.class, MpsatInputPropernessVerificationCommand.class);
        pm.registerClass(Command.class, MpsatOutputPersistencyVerificationCommand.class);
        pm.registerClass(Command.class, MpsatDiInterfaceVerificationCommand.class);
        pm.registerClass(Command.class, MpsatNormalcyVerificationCommand.class);
        pm.registerClass(Command.class, MpsatCscVerificationCommand.class);
        pm.registerClass(Command.class, MpsatUscVerificationCommand.class);
        pm.registerClass(Command.class, MpsatConformationVerificationCommand.class);
        pm.registerClass(Command.class, MpsatCombinedVerificationCommand.class);
        pm.registerClass(Command.class, MpsatPropertyVerificationCommand.class);
        pm.registerClass(Command.class, MpsatAssertionVerificationCommand.class);
        pm.registerClass(Settings.class, MpsatSettings.class);
    }

    @Override
    public String getDescription() {
        return "MPSat verification support";
    }

}
