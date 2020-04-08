package org.workcraft.plugins.mpsat_verification;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.mpsat_verification.commands.*;
import org.workcraft.utils.ScriptableCommandUtils;

@SuppressWarnings("unused")
public class MpsatVerificationPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "MPSat verification plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerSettings(MpsatVerificationSettings.class);

        ScriptableCommandUtils.register(CombinedVerificationCommand.class, "checkStgCombined",
                "combined check of the STG 'work' for consistency, deadlock freeness, input properness, output persistency, and mutex implementability");

        ScriptableCommandUtils.register(ConsistencyVerificationCommand.class, "checkStgConsistency",
                "check the STG 'work' for consistency");

        ScriptableCommandUtils.register(DeadlockFreenessVerificationCommand.class, "checkStgDeadlockFreeness",
                "check the STG (or Petri net) 'work' for deadlock freeness");

        ScriptableCommandUtils.register(InputPropernessVerificationCommand.class, "checkStgInputProperness",
                "check the STG 'work' for input properness");

        ScriptableCommandUtils.register(OutputPersistencyVerificationCommand.class, "checkStgOutputPersistency",
                "check the STG 'work' for output persistency");

        ScriptableCommandUtils.register(CscVerificationCommand.class, "checkStgCsc",
                "check the STG 'work' for complete state coding");

        ScriptableCommandUtils.register(UscVerificationCommand.class, "checkStgUsc",
                "check the STG 'work' for unique state coding");

        ScriptableCommandUtils.register(DiInterfaceVerificationCommand.class, "checkStgDiInterface",
                "check the STG 'work' for delay-insensitive interface");

        ScriptableCommandUtils.register(NormalcyVerificationCommand.class, "checkStgNormalcy",
                "check the STG 'work' for normalcy");

        ScriptableCommandUtils.register(MutexImplementabilityVerificationCommand.class, "checkStgMutexImplementability",
                "check the STG 'work' for implementability of its mutex places");

        ScriptableCommandUtils.register(OutputDeterminacyVerificationCommand.class, "checkStgOutputDeterminacy",
                "check the STG 'work' for output determinacy");

        ScriptableCommandUtils.register(NwayConformationVerificationCommand.class, "checkStgNwayConformation",
                "check all the open STG works for N-way conformation ('null' should be passed as the parameter)");

        ScriptableCommandUtils.registerDataCommand(PlaceRedundancyVerificationCommand.class, "checkStgPlaceRedundancy",
                "check STG (or Petri net) 'work' for redundancy of places in space-separated list 'data'");

        // TODO: Need a way to pass the environment file from the JavaScript wrapper
        pm.registerCommand(ConformationVerificationCommand.class);

        ScriptableCommandUtils.registerDataCommand(ReachAssertionVerificationCommand.class, "checkStgReachAssertion",
                "check STG 'work' for REACH assertion 'data'");

        ScriptableCommandUtils.registerDataCommand(SignalAssertionVerificationCommand.class, "checkStgSignalAssertion",
                "check STG 'work' for signal assertion 'data'");

        ScriptableCommandUtils.registerDataCommand(HandshakeVerificationCommand.class, "checkStgHandshakeProtocol",
                "check STG 'work' following handshake protocol as specified by 'data', e.g. '{req1 req2}{ack12}'");
    }

}
