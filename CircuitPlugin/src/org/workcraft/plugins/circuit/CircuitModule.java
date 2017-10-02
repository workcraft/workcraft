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
import org.workcraft.plugins.circuit.commands.CircuitVerificationCommand;
import org.workcraft.plugins.circuit.commands.ContractComponentTransformationCommand;
import org.workcraft.plugins.circuit.commands.ContractJointTransformationCommand;
import org.workcraft.plugins.circuit.commands.DetachJointTransformationCommand;
import org.workcraft.plugins.circuit.commands.InsertBufferTransformationCommand;
import org.workcraft.plugins.circuit.commands.SplitJointTransformationCommand;
import org.workcraft.plugins.circuit.commands.ToggleBubbleTransformationCommand;
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

        pm.registerClass(Command.class, CircuitLayoutCommand.class);
        pm.registerClass(Command.class, CircuitLayoutPlacementCommand.class);
        pm.registerClass(Command.class, CircuitLayoutRoutingCommand.class);
        pm.registerClass(Settings.class, CircuitLayoutSettings.class);

        pm.registerClass(Command.class, CircuitToStgConversionCommand.class);

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

        pm.registerClass(Command.class, ContractJointTransformationCommand.class);
        pm.registerClass(Command.class, SplitJointTransformationCommand.class);
        pm.registerClass(Command.class, DetachJointTransformationCommand.class);
        pm.registerClass(Command.class, ContractComponentTransformationCommand.class);
        pm.registerClass(Command.class, InsertBufferTransformationCommand.class);
        pm.registerClass(Command.class, ToggleBubbleTransformationCommand.class);

        ScriptableCommandUtils.register(CircuitStatisticsCommand.class, "statCircuit",
                "advanced complexity estimates for the Circuit 'work'");
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
