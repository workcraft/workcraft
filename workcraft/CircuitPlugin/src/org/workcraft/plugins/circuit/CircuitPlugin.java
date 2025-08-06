package org.workcraft.plugins.circuit;

import org.workcraft.Framework;
import org.workcraft.Version;
import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.plugins.circuit.interop.*;
import org.workcraft.plugins.circuit.serialisation.FunctionDeserialiser;
import org.workcraft.plugins.circuit.serialisation.FunctionSerialiser;
import org.workcraft.utils.ScriptableCommandUtils;

@SuppressWarnings("unused")
public class CircuitPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Digital Circuit plugin";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerModelDescriptor(CircuitDescriptor.class);
        pm.registerXmlSerialiser(FunctionSerialiser.class);
        pm.registerXmlDeserialiser(FunctionDeserialiser.class);
        pm.registerSettings(CircuitSettings.class);

        pm.registerExporter(VerilogNetlistExporter.class);
        pm.registerExporter(SystemVerilogAssignExporter.class);
        pm.registerImporter(VerilogImporter.class);
        pm.registerImporter(SystemVerilogImporter.class);
        pm.registerImporter(GenlibImporter.class);

        // Circuit layout settings and commands
        pm.registerSettings(CircuitLayoutSettings.class);

        ScriptableCommandUtils.registerCommand(CircuitLayoutCommand.class, "layoutCircuit",
                "place components and route wires of the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(CircuitLayoutPlacementCommand.class, "layoutCircuitPlacement",
                "place components of the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(CircuitLayoutRoutingCommand.class, "layoutCircuitRouting",
                "route wires of the Circuit 'work'");

        // Transformation commands
        ScriptableCommandUtils.registerCommand(ContractJointTransformationCommand.class, "transformCircuitContractJoint",
                "transform the Circuit 'work' by contracting selected (or all) joints");
        ScriptableCommandUtils.registerCommand(DissolveJointTransformationCommand.class, "transformCircuitDissolveJoint",
                "transform the Circuit 'work' by dissolving selected (or all) joints");
        ScriptableCommandUtils.registerCommand(DetachJointTransformationCommand.class, "transformCircuitDetachJoint",
                "transform the Circuit 'work' by detaching selected (or all) joints");
        ScriptableCommandUtils.registerCommand(ContractComponentTransformationCommand.class, "transformCircuitContractComponent",
                "transform the Circuit 'work' by contracting selected single-input/single-output components");
        ScriptableCommandUtils.registerCommand(InsertBufferTransformationCommand.class, "transformCircuitInsertBuffer",
                "transform the Circuit 'work' by inserting buffers into selected wires");
        ScriptableCommandUtils.registerCommand(ToggleBubbleTransformationCommand.class, "transformCircuitToggleBubble",
                "transform the Circuit 'work' by toggling inversion of selected contacts and outputs of selected components");
        ScriptableCommandUtils.registerCommand(ToggleZeroDelayTransformationCommand.class, "transformCircuitToggleZeroDelay",
                "transform the Circuit 'work' by toggling zero delay of selected inverters and buffers");
        ScriptableCommandUtils.registerCommand(OptimiseZeroDelayTransformationCommand.class, "transformCircuitOptimiseZeroDelay",
                "transform the Circuit 'work' by discarding redundant zero delay attribute for selected (or all) inverters and buffers");
        ScriptableCommandUtils.registerCommand(SplitGateTransformationCommand.class, "transformCircuitSplitGate",
                "transform the Circuit 'work' by splitting selected (or all) complex gates into simple gates");
        ScriptableCommandUtils.registerCommand(PropagateInversionTransformationCommand.class, "transformCircuitPropagateInversion",
                "transform the Circuit 'work' by propagating inversion through selected (or all) gates");
        ScriptableCommandUtils.registerCommand(SquashComponentTransformationCommand.class, "transformCircuitSquashComponent",
                "transform the Circuit 'work' by replacing selected (or all) components with their implementations");
        ScriptableCommandUtils.registerCommand(UpdateComponentInterfaceTransformationCommand.class, "transformCircuitUpdateComponentInterface",
                "transform the Circuit 'work' by updating selected (or all) components from their refinement models");
        ScriptableCommandUtils.registerCommand(ProxyContactTransformationCommand.class, "transformCircuitProxyDriverContact",
                "transform the Circuit 'work' by creating proxies for selected driver contacts");
        ScriptableCommandUtils.registerCommand(CollapseProxyTransformationCommand.class, "transformCircuitCollapseProxy",
                "transform the Circuit 'work' by collapsing selected (or all) proxy contacts");

        // Statistics commands
        ScriptableCommandUtils.registerCommand(StatisticsCommand.class, "statCircuit",
                "advanced complexity estimates for the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(RefinementStatisticsCommand.class, "statCircuitRefinement",
                "refinement dependencies for the Circuit 'work'");

        // Conversion commands
        ScriptableCommandUtils.registerCommand(CircuitToStgConversionCommand.class, "convertCircuitToStg",
                "convert the Circuit 'work' into a new STG work");
        ScriptableCommandUtils.registerCommand(CircuitToStgWithEnvironmentConversionCommand.class, "convertCircuitToStgWithEnvironment",
                "convert the Circuit 'work' and its environment into a new STG work");

        // Verification commands
        ScriptableCommandUtils.registerCommand(CombinedVerificationCommand.class, "checkCircuitCombined",
                "combined check of the Circuit 'work' for all essential properties"
                        + " (conformation to environment, output persistency, deadlock freeness)");
        ScriptableCommandUtils.registerCommand(ConformationVerificationCommand.class, "checkCircuitConformation",
                "check the Circuit 'work' for conformation to environment");
        ScriptableCommandUtils.registerCommand(OutputPersistencyVerificationCommand.class, "checkCircuitOutputPersistency",
                "check the Circuit 'work' for output persistency");
        ScriptableCommandUtils.registerCommand(DeadlockFreenessVerificationCommand.class, "checkCircuitDeadlockFreeness",
                "check the Circuit 'work' for deadlock freeness");

        ScriptableCommandUtils.registerCommand(StrictImplementationVerificationCommand.class, "checkCircuitStrictImplementation",
                "check the Circuit 'work' for strict implementation of its signals according to the environment");
        ScriptableCommandUtils.registerCommand(BinateImplementationVerificationCommand.class, "checkCircuitBinateImplementation",
                "check the Circuit 'work' for correct implementation of its binate functions");
        ScriptableCommandUtils.registerCommand(RefinementVerificationCommand.class, "checkCircuitRefinement",
                "check the Circuit 'work' is a refinement of its environment STG");

        ScriptableCommandUtils.registerDataCommand(ReachAssertionVerificationCommand.class, "checkCircuitReachAssertion",
                "check the Circuit 'work' for REACH assertion 'data'");
        ScriptableCommandUtils.registerDataCommand(SignalAssertionVerificationCommand.class, "checkCircuitSignalAssertion",
                "check the Circuit 'work' for signal assertion 'data'");
        ScriptableCommandUtils.registerDataCommand(SpotAssertionVerificationCommand.class, "checkCircuitSpotAssertion",
                "check the Circuit 'work' for SPOT assertion 'data'");

        // Forced init attributes and Reset insertion
        ScriptableCommandUtils.registerCommand(ForcedInitInputPortsTagCommand.class, "tagCircuitForcedInitInputPorts",
                "force init all input ports in the Circuit 'work' (environment must initialise them)");
        ScriptableCommandUtils.registerCommand(ForcedInitProblematicPinsTagCommand.class, "tagCircuitForcedInitProblematicPins",
                "force init output pins with problematic initial state in the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(ForcedInitSequentialPinsTagCommand.class, "tagCircuitForcedInitSequentialPins",
                "force init output pins of sequential gates in the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(ForcedInitAutoAppendTagCommand.class, "tagCircuitForcedInitAutoAppend",
                "auto-append forced init pins as necessary to complete initialisation of the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(ForcedInitAutoDiscardTagCommand.class, "tagCircuitForcedInitAutoDiscard",
                "auto-discard forced init pins that are redundant for initialisation of the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(ForcedInitClearAllTagCommand.class, "tagCircuitForcedInitClearAll",
                "clear all forced init input ports and output pins in the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(ResetActiveLowInsertionCommand.class, "insertCircuitResetActiveLow",
                "insert active-low reset into the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(ResetActiveHighInsertionCommand.class, "insertCircuitResetActiveHigh",
                "insert active-high reset into the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(ResetVerificationCommand.class, "checkCircuitReset",
                "check if the Circuit 'work' is correctly initialised via forced input ports");

        // Path breaker attributes and Scan insertion
        ScriptableCommandUtils.registerCommand(PathBreakerSelfloopPinsTagCommand.class, "tagCircuitPathBreakerSelfloopPins",
                "path breaker output pins within self-loops in the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(PathBreakerAutoAppendTagCommand.class, "tagCircuitPathBreakerAutoAppend",
                "auto-append path breaker pins as necessary to complete cycle breaking in the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(PathBreakerAutoDiscardTagCommand.class, "tagCircuitPathBreakerAutoDiscard",
                "auto-discard path breaker pins that are redundant for cycle breaking in the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(PathBreakerClearAllTagCommand.class, "tagCircuitPathBreakerClearAll",
                "clear all path breaker pins in the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(TestableGateInsertionCommand.class, "insertCircuitTestableGates",
                "insert testable buffers/inverters for path breaker components in the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(ScanInsertionCommand.class, "insertCircuitScan",
                "insert scan for path breaker components into the Circuit 'work'");
        ScriptableCommandUtils.registerCommand(CycleFreenessVerificationCommand.class, "checkCircuitCycles",
                "check if the Circuit 'work' is free from cyclic paths");

        // Fork buffering
        ScriptableCommandUtils.registerCommand(BufferHighFanoutTransformationCommand.class, "transformCircuitBufferHighFanout",
                "buffer forks with high fanout in the Circuit 'work'");

        // Mutex protocol conversion
        ScriptableCommandUtils.registerCommand(MutexProtocolEarlyTransformationCommand.class, "transformCircuitMutexProtocolEarly",
                "transform the Circuit 'work' by setting early protocol for selected (or all) mutex components");
        ScriptableCommandUtils.registerCommand(MutexProtocolLateTransformationCommand.class, "transformCircuitMutexProtocolLate",
                "transform the Circuit 'work' by setting late protocol for selected (or all) mutex components");
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.circuit.CircuitModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.circuit.CircuitDescriptor\"/>");

        cm.registerContextualReplacement(v310, VisualCircuit.class.getName(), "VisualCircuitComponent",
                "<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"C_ELEMENT\"/>",
                "<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"GATE\"/>");

        cm.registerContextualReplacement(v310, VisualCircuit.class.getName(), "VisualCircuitComponent",
                "<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"BUFFER\"/>",
                "<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"GATE\"/>");

        cm.registerContextualReplacement(v310, Circuit.class.getName(), "Contact",
                "<property class=\"boolean\" name=\"initOne\" value=\"(.*?)\"/>",
                "<property class=\"boolean\" name=\"initToOne\" value=\"$1\"/>");

    }

}
