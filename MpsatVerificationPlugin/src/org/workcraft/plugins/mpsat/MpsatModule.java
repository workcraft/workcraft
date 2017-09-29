package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.Command;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.mpsat.commands.MpsatAssertionVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatCombinedVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatConformationVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatConsistencyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatCscConflictResolutionCommand;
import org.workcraft.plugins.mpsat.commands.MpsatCscVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatDeadlockFreenessVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatDiInterfaceVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatMutexImplementabilityVerificationCommand;
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

        pm.registerClass(Settings.class, MpsatSettings.class);

        ScriptableCommandUtils.register(MpsatCscConflictResolutionCommand.class, "resolveCscConflictMpsat",
                "resolve complete state coding conflicts with MPSat backend");

        ScriptableCommandUtils.register(MpsatCombinedVerificationCommand.class, "checkStgCombined",
                "combined check of the STG for consistency, deadlock freeness, input properness and output persistency");
        ScriptableCommandUtils.register(MpsatConsistencyVerificationCommand.class, "checkStgConsistency",
                "check the STG for consistency");
        ScriptableCommandUtils.register(MpsatDeadlockFreenessVerificationCommand.class, "checkStgDeadlockFreeness",
                "check the STG (or Petri net) for deadlock freeness");
        ScriptableCommandUtils.register(MpsatInputPropernessVerificationCommand.class, "checkStgInputProperness",
                "check the STG for input properness");
        ScriptableCommandUtils.register(MpsatOutputPersistencyVerificationCommand.class, "checkStgOutputPersistency",
                "check the STG for output persistency");
        ScriptableCommandUtils.register(MpsatCscVerificationCommand.class, "checkStgCsc",
                "check the STG for complete state coding");
        ScriptableCommandUtils.register(MpsatUscVerificationCommand.class, "checkStgUsc",
                "check the STG for unique state coding");
        ScriptableCommandUtils.register(MpsatDiInterfaceVerificationCommand.class, "checkStgDiInterface",
                "check the STG for delay-insensitive interface");
        ScriptableCommandUtils.register(MpsatNormalcyVerificationCommand.class, "checkStgNormalcy",
                "check the STG for normalcy");
        ScriptableCommandUtils.register(MpsatMutexImplementabilityVerificationCommand.class, "checkStgMutexImplementability",
                "check the STG for implementability of its mutex places");

        pm.registerClass(Command.class, MpsatConformationVerificationCommand.class);
        pm.registerClass(Command.class, MpsatPropertyVerificationCommand.class);
        pm.registerClass(Command.class, MpsatAssertionVerificationCommand.class);
    }

    @Override
    public String getDescription() {
        return "MPSat verification support";
    }

}
