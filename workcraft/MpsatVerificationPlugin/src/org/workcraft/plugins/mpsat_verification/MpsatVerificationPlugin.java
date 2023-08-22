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

        // Core verification commands
        ScriptableCommandUtils.registerCommand(CombinedVerificationCommand.class, "checkStgCombined",
                "combined check of the STG 'work' for consistency, deadlock freeness, input properness, output persistency, and mutex implementability");
        ScriptableCommandUtils.registerCommand(ConsistencyVerificationCommand.class, "checkStgConsistency",
                "check the STG 'work' for consistency");
        ScriptableCommandUtils.registerCommand(DeadlockFreenessVerificationCommand.class, "checkStgDeadlockFreeness",
                "check the STG (or Petri net) 'work' for deadlock freeness");
        ScriptableCommandUtils.registerCommand(InputPropernessVerificationCommand.class, "checkStgInputProperness",
                "check the STG 'work' for input properness");
        ScriptableCommandUtils.registerCommand(MutexImplementabilityVerificationCommand.class, "checkStgMutexImplementability",
                "check the STG 'work' for implementability of its mutex places");
        ScriptableCommandUtils.registerCommand(OutputPersistencyVerificationCommand.class, "checkStgOutputPersistency",
                "check the STG 'work' for output persistency");
        ScriptableCommandUtils.registerCommand(LocalSelfTriggeringVerificationCommand.class, "checkStgLocalSelfTriggering",
                "check the STG 'work' for absence of local self-triggering");
        ScriptableCommandUtils.registerCommand(DiInterfaceVerificationCommand.class, "checkStgDiInterface",
                "check the STG 'work' for delay-insensitive interface");
        ScriptableCommandUtils.registerCommand(OutputDeterminacyVerificationCommand.class, "checkStgOutputDeterminacy",
                "check the STG 'work' for output determinacy");

        // Auxiliary verification commands
        ScriptableCommandUtils.registerCommand(NormalcyVerificationCommand.class, "checkStgNormalcy",
                "check the STG 'work' for normalcy");

        // Commands for verification of encoding conflicts
        ScriptableCommandUtils.registerCommand(CscVerificationCommand.class, "checkStgCsc",
                "check the STG 'work' for complete state coding");
        ScriptableCommandUtils.registerCommand(UscVerificationCommand.class, "checkStgUsc",
                "check the STG 'work' for unique state coding");

        // Commands with user-defined parameters
        ScriptableCommandUtils.registerDataCommand(PlaceRedundancyVerificationCommand.class, "checkStgPlaceRedundancy",
                "check the STG (or Petri net) 'work' for redundancy of places in space-separated list 'data'");
        ScriptableCommandUtils.registerDataCommand(ConformationVerificationCommand.class, "checkStgConformation",
                "check the STG 'work' for conformation to the STG specified by file name 'data'");
        ScriptableCommandUtils.registerDataCommand(NwayConformationVerificationCommand.class, "checkStgNwayConformation",
                "check the STGs specified by space-separated list of file names 'data' for N-way conformation ('work' parameter is ignored)");
        ScriptableCommandUtils.registerDataCommand(RefinementVerificationCommand.class, "checkStgRefinement",
                "check the STG 'work' is a refinement of the STG specified by file name 'data'");
        ScriptableCommandUtils.registerDataCommand(RelaxedRefinementVerificationCommand.class, "checkStgRefinementRelaxed",
                "check the STG 'work' is a relaxed refinement (allows concurrency reduction) of the STG specified by file name 'data'");
        ScriptableCommandUtils.registerDataCommand(HandshakeVerificationCommand.class, "checkStgHandshakeProtocol",
                "check the STG 'work' for following a handshake protocol as specified by 'data', e.g. '{req1 req2} {ack12}'");
        ScriptableCommandUtils.registerDataCommand(ReachAssertionVerificationCommand.class, "checkStgReachAssertion",
                "check the STG 'work' for REACH assertion 'data'");
        ScriptableCommandUtils.registerDataCommand(SignalAssertionVerificationCommand.class, "checkStgSignalAssertion",
                "check the STG 'work' for signal assertion 'data'");
        ScriptableCommandUtils.registerDataCommand(SpotAssertionVerificationCommand.class, "checkStgSpotAssertion",
                "check the STG 'work' for SPOT assertion 'data'");
    }

}
