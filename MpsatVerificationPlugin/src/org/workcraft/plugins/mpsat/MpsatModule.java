package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.mpsat.commands.AssertionVerificationCommand;
import org.workcraft.plugins.mpsat.commands.CombinedVerificationCommand;
import org.workcraft.plugins.mpsat.commands.ConformationVerificationCommand;
import org.workcraft.plugins.mpsat.commands.ConsistencyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.CscConflictResolutionCommand;
import org.workcraft.plugins.mpsat.commands.CscVerificationCommand;
import org.workcraft.plugins.mpsat.commands.DeadlockVerificationCommand;
import org.workcraft.plugins.mpsat.commands.DiInterfaceVerificationCommand;
import org.workcraft.plugins.mpsat.commands.InputPropernessVerificationCommand;
import org.workcraft.plugins.mpsat.commands.NormalcyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.OutputPersistencyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.PropertyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.UscVerificationCommand;

public class MpsatModule implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Command.class, CscConflictResolutionCommand.class);
        pm.registerClass(Command.class, ConsistencyVerificationCommand.class);
        pm.registerClass(Command.class, DeadlockVerificationCommand.class);
        pm.registerClass(Command.class, InputPropernessVerificationCommand.class);
        pm.registerClass(Command.class, OutputPersistencyVerificationCommand.class);
        pm.registerClass(Command.class, DiInterfaceVerificationCommand.class);
        pm.registerClass(Command.class, NormalcyVerificationCommand.class);
        pm.registerClass(Command.class, CscVerificationCommand.class);
        pm.registerClass(Command.class, UscVerificationCommand.class);
        pm.registerClass(Command.class, ConformationVerificationCommand.class);
        pm.registerClass(Command.class, CombinedVerificationCommand.class);
        pm.registerClass(Command.class, PropertyVerificationCommand.class);
        pm.registerClass(Command.class, AssertionVerificationCommand.class);
        pm.registerClass(Settings.class, MpsatUtilitySettings.class);
    }

    @Override
    public String getDescription() {
        return "MPSat verification support";
    }

}
