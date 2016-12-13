package org.workcraft.plugins.circuit;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Command;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.circuit.interop.GenlibImporter;
import org.workcraft.plugins.circuit.interop.SdcExporter;
import org.workcraft.plugins.circuit.interop.VerilogExporter;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.serialisation.FunctionDeserialiser;
import org.workcraft.plugins.circuit.serialisation.FunctionSerialiser;
import org.workcraft.plugins.circuit.tools.ToggleBubbleTransformationCommand;
import org.workcraft.plugins.circuit.tools.InsertBufferTransformationCommand;
import org.workcraft.plugins.circuit.tools.CircuitConformationVerificationCommand;
import org.workcraft.plugins.circuit.tools.CircuitDeadlockVerificationCommand;
import org.workcraft.plugins.circuit.tools.CircuitPersistencyVerificationCommand;
import org.workcraft.plugins.circuit.tools.CircuitVerificationCommand;
import org.workcraft.plugins.circuit.tools.CircuitLayoutCommand;
import org.workcraft.plugins.circuit.tools.ContractComponentTransformationCommand;
import org.workcraft.plugins.circuit.tools.CircuitPropertyVerificationCommand;
import org.workcraft.plugins.circuit.tools.CircuitAssertionVerificationCommand;
import org.workcraft.plugins.circuit.tools.ContractJointTransformationCommand;
import org.workcraft.plugins.circuit.tools.DetachJointTransformationCommand;
import org.workcraft.plugins.circuit.tools.SplitJointTransformationCommand;
import org.workcraft.plugins.circuit.tools.CircuitToStgConversionCommand;
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
        pm.registerClass(Command.class, CircuitLayoutCommand.class);
        pm.registerClass(Command.class, CircuitToStgConversionCommand.class);

        pm.registerClass(Command.class, CircuitConformationVerificationCommand.class);
        pm.registerClass(Command.class, CircuitDeadlockVerificationCommand.class);
        pm.registerClass(Command.class, CircuitPersistencyVerificationCommand.class);
        pm.registerClass(Command.class, CircuitVerificationCommand.class);
        pm.registerClass(Command.class, CircuitPropertyVerificationCommand.class);
        pm.registerClass(Command.class, CircuitAssertionVerificationCommand.class);

        pm.registerClass(Command.class, ContractJointTransformationCommand.class);
        pm.registerClass(Command.class, SplitJointTransformationCommand.class);
        pm.registerClass(Command.class, DetachJointTransformationCommand.class);
        pm.registerClass(Command.class, ContractComponentTransformationCommand.class);
        pm.registerClass(Command.class, InsertBufferTransformationCommand.class);
        pm.registerClass(Command.class, ToggleBubbleTransformationCommand.class);

        pm.registerClass(ModelDescriptor.class, CircuitDescriptor.class);
        pm.registerClass(XMLSerialiser.class, FunctionSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, FunctionDeserialiser.class);
        pm.registerClass(Settings.class, CircuitSettings.class);
        pm.registerClass(Exporter.class, VerilogExporter.class);
        pm.registerClass(Importer.class, VerilogImporter.class);
        pm.registerClass(Importer.class, GenlibImporter.class);
        pm.registerClass(Exporter.class, SdcExporter.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.circuit.CircuitModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.circuit.CircuitDescriptor\"/>");

        cm.registerContextualReplacement(VisualCircuit.class.getName(), "VisualCircuitComponent",
                "<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"C_ELEMENT\"/>",
                "<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"GATE\"/>");

        cm.registerContextualReplacement(VisualCircuit.class.getName(), "VisualCircuitComponent",
                "<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"BUFFER\"/>",
                "<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"GATE\"/>");

        cm.registerContextualReplacement(Circuit.class.getName(), "Contact",
                "<property class=\"boolean\" name=\"initOne\" value=\"(.*?)\"/>",
                "<property class=\"boolean\" name=\"initToOne\" value=\"$1\"/>");

    }

}
