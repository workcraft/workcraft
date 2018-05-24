package org.workcraft.plugins.circuit;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Version;
import org.workcraft.commands.Command;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.circuit.commands.CircuitAssertionVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitConformationVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitDeadlockFreenessVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitLayoutCommand;
import org.workcraft.plugins.circuit.commands.CircuitLayoutPlacementCommand;
import org.workcraft.plugins.circuit.commands.CircuitLayoutRoutingCommand;
import org.workcraft.plugins.circuit.commands.CircuitLayoutSettings;
import org.workcraft.plugins.circuit.commands.CircuitOutputPersistencyVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitPropertyVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitStatisticsCommand;
import org.workcraft.plugins.circuit.commands.CircuitStrictImplementationVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitToStgConversionCommand;
import org.workcraft.plugins.circuit.commands.CircuitToStgWithEnvironmentConversionCommand;
import org.workcraft.plugins.circuit.commands.CircuitVerificationCommand;
import org.workcraft.plugins.circuit.commands.ContractComponentTransformationCommand;
import org.workcraft.plugins.circuit.commands.ContractJointTransformationCommand;
import org.workcraft.plugins.circuit.commands.DetachJointTransformationCommand;
import org.workcraft.plugins.circuit.commands.InsertBufferTransformationCommand;
import org.workcraft.plugins.circuit.commands.PropagateInversionTransformationCommand;
import org.workcraft.plugins.circuit.commands.SplitGateTransformationCommand;
import org.workcraft.plugins.circuit.commands.SplitJointTransformationCommand;
import org.workcraft.plugins.circuit.commands.ToggleBubbleTransformationCommand;
import org.workcraft.plugins.circuit.commands.ToggleZeroDelayTransformationCommand;
import org.workcraft.plugins.circuit.interop.GenlibImporter;
import org.workcraft.plugins.circuit.interop.SdcExporter;
import org.workcraft.plugins.circuit.interop.VerilogExporter;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.serialisation.FunctionDeserialiser;
import org.workcraft.plugins.circuit.serialisation.FunctionSerialiser;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class CircuitModule implements Module {

    @Override
    public String getDescription() {
        return "Gate-level circuit model";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerClass(ModelDescriptor.class, CircuitDescriptor.class);
        pm.registerClass(XMLSerialiser.class, FunctionSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, FunctionDeserialiser.class);
        pm.registerClass(Settings.class, CircuitSettings.class);

        pm.registerClass(Exporter.class, VerilogExporter.class);
        pm.registerClass(Importer.class, VerilogImporter.class);
        pm.registerClass(Importer.class, GenlibImporter.class);
        pm.registerClass(Exporter.class, SdcExporter.class);

        ScriptableCommandUtils.register(CircuitLayoutCommand.class, "layoutCircuit",
                "place components and route wires of the Circuit 'work'");
        ScriptableCommandUtils.register(CircuitLayoutPlacementCommand.class, "layoutCircuitPlacement",
                "place components of the Circuit 'work'");
        ScriptableCommandUtils.register(CircuitLayoutRoutingCommand.class, "layoutCircuitRouting",
                "route wires of the Circuit 'work'");
        pm.registerClass(Settings.class, CircuitLayoutSettings.class);

        ScriptableCommandUtils.register(ContractJointTransformationCommand.class, "transformCircuitContractJoint",
                "transform the given Circuit 'work' by contracting selected (or all) joints");
        ScriptableCommandUtils.register(SplitJointTransformationCommand.class, "transformCircuitSplitJoint",
                "transform the given Circuit 'work' by splitting selected (or all) joints");
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
                "combined check of the Circuit 'work' for deadlock freeness, conformation to environment and output persistency");
        ScriptableCommandUtils.register(CircuitConformationVerificationCommand.class, "checkCircuitConformation",
                "check the Circuit 'work' for conformation to environment");
        ScriptableCommandUtils.register(CircuitDeadlockFreenessVerificationCommand.class, "checkCircuitDeadlockFreeness",
                "check the Circuit 'work' for deadlock freeness");
        ScriptableCommandUtils.register(CircuitOutputPersistencyVerificationCommand.class, "checkCircuitOutputPersistency",
                "check the Circuit 'work' for output persistency");
        ScriptableCommandUtils.register(CircuitStrictImplementationVerificationCommand.class, "checkCircuitStrictImplementation",
                "check the Circuit 'work' for strict implementation of its signals according to the environment");

        pm.registerClass(Command.class, CircuitPropertyVerificationCommand.class);
        pm.registerClass(Command.class, CircuitAssertionVerificationCommand.class);
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
