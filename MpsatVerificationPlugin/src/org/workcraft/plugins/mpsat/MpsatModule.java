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
import org.workcraft.plugins.mpsat.commands.MpsatInputPropernessVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatMutexImplementabilityVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatNormalcyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatOutputPersistencyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatPlaceRedundancyVerificationCommand;
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
                "combined check of the STG 'work' for consistency, deadlock freeness, input properness and output persistency");
        ScriptableCommandUtils.register(MpsatConsistencyVerificationCommand.class, "checkStgConsistency",
                "check the STG 'work' for consistency");
        ScriptableCommandUtils.register(MpsatDeadlockFreenessVerificationCommand.class, "checkStgDeadlockFreeness",
                "check the STG (or Petri net) 'work' for deadlock freeness");
        ScriptableCommandUtils.register(MpsatInputPropernessVerificationCommand.class, "checkStgInputProperness",
                "check the STG 'work' for input properness");
        ScriptableCommandUtils.register(MpsatOutputPersistencyVerificationCommand.class, "checkStgOutputPersistency",
                "check the STG 'work' for output persistency");
        ScriptableCommandUtils.register(MpsatCscVerificationCommand.class, "checkStgCsc",
                "check the STG 'work' for complete state coding");
        ScriptableCommandUtils.register(MpsatUscVerificationCommand.class, "checkStgUsc",
                "check the STG 'work' for unique state coding");
        ScriptableCommandUtils.register(MpsatDiInterfaceVerificationCommand.class, "checkStgDiInterface",
                "check the STG 'work' for delay-insensitive interface");
        ScriptableCommandUtils.register(MpsatNormalcyVerificationCommand.class, "checkStgNormalcy",
                "check the STG 'work' for normalcy");
        ScriptableCommandUtils.register(MpsatMutexImplementabilityVerificationCommand.class, "checkStgMutexImplementability",
                "check the STG 'work' for implementability of its mutex places");

        // TODO: Need a way to pass the list of places from the JavaScript wrapper
        pm.registerClass(Command.class, MpsatPlaceRedundancyVerificationCommand.class);
        // TODO: Need a way to pass the environment file from the JavaScript wrapper
        pm.registerClass(Command.class, MpsatConformationVerificationCommand.class);
        //pm.registerClass(Command.class, MpsatNwayConformationVerificationCommand.class);
        pm.registerClass(Command.class, MpsatPropertyVerificationCommand.class);
        pm.registerClass(Command.class, MpsatAssertionVerificationCommand.class);
    }

    @Override
    public String getDescription() {
        return "MPSat verification support";
    }

}
