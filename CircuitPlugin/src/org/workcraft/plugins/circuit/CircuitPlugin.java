package org.workcraft.plugins.circuit;

import org.workcraft.Framework;
import org.workcraft.Version;
import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.plugins.circuit.interop.GenlibImporter;
import org.workcraft.plugins.circuit.interop.VerilogExporter;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
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

        pm.registerExporter(VerilogExporter.class);
        pm.registerImporter(VerilogImporter.class);
        pm.registerImporter(GenlibImporter.class);

        ScriptableCommandUtils.register(CircuitLayoutCommand.class, "layoutCircuit",
                "place components and route wires of the Circuit 'work'");
        ScriptableCommandUtils.register(CircuitLayoutPlacementCommand.class, "layoutCircuitPlacement",
                "place components of the Circuit 'work'");
        ScriptableCommandUtils.register(CircuitLayoutRoutingCommand.class, "layoutCircuitRouting",
                "route wires of the Circuit 'work'");
        pm.registerSettings(CircuitLayoutSettings.class);

        ScriptableCommandUtils.register(ContractJointTransformationCommand.class, "transformCircuitContractJoint",
                "transform the given Circuit 'work' by contracting selected (or all) joints");
        ScriptableCommandUtils.register(DissolveJointTransformationCommand.class, "transformCircuitDissolveJoint",
                "transform the given Circuit 'work' by dissolving selected (or all) joints");
        ScriptableCommandUtils.register(DetachJointTransformationCommand.class, "transformCircuitDetachJoint",
                "transform the given Circuit 'work' by detaching selected (or all joints)");
        ScriptableCommandUtils.register(ContractComponentTransformationCommand.class, "transformCircuitContractComponent",
                "transform the given Circuit 'work' by contracting selected single-input/single-output components");
        ScriptableCommandUtils.register(InsertBufferTransformationCommand.class, "transformCircuitInsertBuffer",
                "transform the given Circuit 'work' by inserting buffers into selected wires");
        ScriptableCommandUtils.register(ToggleBubbleTransformationCommand.class, "transformCircuitToggleBubble",
                "transform the given Circuit 'work' by toggling inversion of selected contacts and outputs of selected components");
        ScriptableCommandUtils.register(ToggleZeroDelayTransformationCommand.class, "transformCircuitToggleZeroDelay",
                "transform the given Circuit 'work' by toggling zero delay of selected inverters and buffers");
        ScriptableCommandUtils.register(SplitGateTransformationCommand.class, "transformCircuitSplitGate",
                "transform the given Circuit 'work' by splitting selected (or all) complex gates into simple gates");
        ScriptableCommandUtils.register(PropagateInversionTransformationCommand.class, "transformCircuitPropagateInversion",
                "transform the given Circuit 'work' by propagating inversion through selected (or all) gates");

        ScriptableCommandUtils.register(CircuitStatisticsCommand.class, "statCircuit",
                "advanced complexity estimates for the Circuit 'work'");

        ScriptableCommandUtils.register(CircuitToStgConversionCommand.class, "convertCircuitToStg",
                "convert the given Circuit 'work' into a new STG work");
        ScriptableCommandUtils.register(CircuitToStgWithEnvironmentConversionCommand.class, "convertCircuitToStgWithEnvironment",
                "convert the given Circuit 'work' and its environment into a new STG work");

        ScriptableCommandUtils.register(CircuitVerificationCommand.class, "checkCircuitCombined",
                "combined check of the Circuit 'work' for conformation to environment, deadlock freeness, and output persistency");
        ScriptableCommandUtils.register(CircuitConformationVerificationCommand.class, "checkCircuitConformation",
                "check the Circuit 'work' for conformation to environment");
        ScriptableCommandUtils.register(CircuitDeadlockFreenessVerificationCommand.class, "checkCircuitDeadlockFreeness",
                "check the Circuit 'work' for deadlock freeness");
        ScriptableCommandUtils.register(CircuitOutputPersistencyVerificationCommand.class, "checkCircuitOutputPersistency",
                "check the Circuit 'work' for output persistency");
        ScriptableCommandUtils.register(CircuitStrictImplementationVerificationCommand.class, "checkCircuitStrictImplementation",
                "check the Circuit 'work' for strict implementation of its signals according to the environment");

        pm.registerCommand(CircuitPropertyVerificationCommand.class);
        pm.registerCommand(CircuitAssertionVerificationCommand.class);

        ScriptableCommandUtils.register(ClearForceInitCommand.class, "resetCircuitClearForceInit",
                "clear force init for all input ports and output pins in the Circuit 'work'");
        ScriptableCommandUtils.register(ForceInitInputPortsCommand.class, "resetCircuitForceInitInputPorts",
                "force init all input ports in the Circuit 'work'  (environment must initialise them)");
        ScriptableCommandUtils.register(ForceInitSelfLoopsCommand.class, "resetCircuitForceInitSelfLoops",
                "force init all self-loops in the Circuit 'work'");
        ScriptableCommandUtils.register(ForceInitSequentialGatesCommand.class, "resetCircuitForceInitSequentialGates",
                "force init all sequential gates in the Circuit 'work'");

        ScriptableCommandUtils.register(ProcessRedundantForceInitPinsCommand.class, "resetCircuitProcessRedundantForceInitPins",
                "remove force init from pins if redundant for initialisation of the Circuit 'work'");
        ScriptableCommandUtils.register(ProcessNecessaryForceInitPinsCommand.class, "resetCircuitProcessNecessaryForceInitPins",
                "add force init to pins if necessary to complete initialisation of the Circuit 'work'");

        ScriptableCommandUtils.register(CircuitResetActiveLowCommand.class, "resetCircuitInsertActiveLow",
                "insert active-low reset into the Circuit 'work'");
        ScriptableCommandUtils.register(CircuitResetActiveHighCommand.class, "resetCircuitInsertActiveHigh",
                "insert active-high reset into the Circuit 'work'");

        ScriptableCommandUtils.register(CircuitResetVerificationCommand.class, "checkCircuitReset",
                "check if the Circuit 'work' is correctly initialised via input ports");
        ScriptableCommandUtils.register(CircuitCycleFreenessVerificationCommand.class, "checkCircuitCycles",
                "check if the Circuit 'work' is free from cyclic paths");

        ScriptableCommandUtils.register(CircuitInsertLoopbreakerBuffersCommand.class, "insertCircuitLoopbreakerBuffers",
                "insert loop breaker buffers into the Circuit 'work'");
        ScriptableCommandUtils.register(CircuitInsertPathbreakerScanCommand.class, "insertCircuitScan",
                "insert scan for path breaker components into the Circuit 'work' ");
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
