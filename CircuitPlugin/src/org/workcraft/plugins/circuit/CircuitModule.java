package org.workcraft.plugins.circuit;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
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
import org.workcraft.plugins.circuit.tools.BubbleToggleTool;
import org.workcraft.plugins.circuit.tools.BufferInsertionTool;
import org.workcraft.plugins.circuit.tools.CircuitConformationChecker;
import org.workcraft.plugins.circuit.tools.CircuitDeadlockChecker;
import org.workcraft.plugins.circuit.tools.CircuitHazardChecker;
import org.workcraft.plugins.circuit.tools.CircuitChecker;
import org.workcraft.plugins.circuit.tools.CircuitLayoutTool;
import org.workcraft.plugins.circuit.tools.ComponentContractionTool;
import org.workcraft.plugins.circuit.tools.CircuitPropertyChecker;
import org.workcraft.plugins.circuit.tools.CircuitAssertionChecker;
import org.workcraft.plugins.circuit.tools.JointContractionTool;
import org.workcraft.plugins.circuit.tools.JointDetachTool;
import org.workcraft.plugins.circuit.tools.JointSplitTool;
import org.workcraft.plugins.circuit.tools.StgGeneratorTool;
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
        pm.registerClass(Tool.class, CircuitLayoutTool.class);
        pm.registerClass(Tool.class, StgGeneratorTool.class);

        pm.registerClass(Tool.class, CircuitConformationChecker.class);
        pm.registerClass(Tool.class, CircuitDeadlockChecker.class);
        pm.registerClass(Tool.class, CircuitHazardChecker.class);
        pm.registerClass(Tool.class, CircuitChecker.class);
        pm.registerClass(Tool.class, CircuitPropertyChecker.class);
        pm.registerClass(Tool.class, CircuitAssertionChecker.class);

        pm.registerClass(Tool.class, JointContractionTool.class);
        pm.registerClass(Tool.class, JointSplitTool.class);
        pm.registerClass(Tool.class, JointDetachTool.class);
        pm.registerClass(Tool.class, ComponentContractionTool.class);
        pm.registerClass(Tool.class, BufferInsertionTool.class);
        pm.registerClass(Tool.class, BubbleToggleTool.class);

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
