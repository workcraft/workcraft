package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.mpsat.tools.CscResolutionCommand;
import org.workcraft.plugins.mpsat.tools.CombinedVerificationCommand;
import org.workcraft.plugins.mpsat.tools.ConformationVerificationCommand;
import org.workcraft.plugins.mpsat.tools.ConsistencyVerificationCommand;
import org.workcraft.plugins.mpsat.tools.CscVerificationCommand;
import org.workcraft.plugins.mpsat.tools.PropertyVerificationCommand;
import org.workcraft.plugins.mpsat.tools.AssertionVerificationCommand;
import org.workcraft.plugins.mpsat.tools.DeadlockVerificationCommand;
import org.workcraft.plugins.mpsat.tools.DiInterfaceVerificationCommand;
import org.workcraft.plugins.mpsat.tools.InputPropernessVerificationCommand;
import org.workcraft.plugins.mpsat.tools.NormalcyVerificationCommand;
import org.workcraft.plugins.mpsat.tools.OutputPersistencyVerificationCommand;
import org.workcraft.plugins.mpsat.tools.UscVerificationCommand;

public class MpsatModule implements Module {

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Command.class, CscResolutionCommand.class);
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
