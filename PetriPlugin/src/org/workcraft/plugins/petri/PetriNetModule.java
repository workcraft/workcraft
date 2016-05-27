package org.workcraft.plugins.petri;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.plugins.petri.serialization.ReadArcDeserialiser;
import org.workcraft.plugins.petri.serialization.ReadArcSerialiser;
import org.workcraft.plugins.petri.tools.CollapseReplicaTool;
import org.workcraft.plugins.petri.tools.DirectedArcToReadArcConverterTool;
import org.workcraft.plugins.petri.tools.DualArcToReadArcConverterTool;
import org.workcraft.plugins.petri.tools.MergePlaceTool;
import org.workcraft.plugins.petri.tools.ProxyDirectedArcPlaceTool;
import org.workcraft.plugins.petri.tools.ProxyReadArcPlaceTool;
import org.workcraft.plugins.petri.tools.ReadArcToDualArcConverterTool;
import org.workcraft.plugins.petri.tools.TransitionContractorTool;
import org.workcraft.plugins.petri.tools.TransitionMergerTool;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class PetriNetModule implements Module {

    @Override
    public String getDescription() {
        return "Petri Net";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerClass(ModelDescriptor.class, PetriNetDescriptor.class);

        pm.registerClass(XMLSerialiser.class, ReadArcSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, ReadArcDeserialiser.class);

        pm.registerClass(Tool.class, TransitionContractorTool.class);
        pm.registerClass(Tool.class, DirectedArcToReadArcConverterTool.class);
        pm.registerClass(Tool.class, DualArcToReadArcConverterTool.class);
        pm.registerClass(Tool.class, ReadArcToDualArcConverterTool.class);
        pm.registerClass(Tool.class, CollapseReplicaTool.class);
        pm.registerClass(Tool.class, ProxyDirectedArcPlaceTool.class);
        pm.registerClass(Tool.class, ProxyReadArcPlaceTool.class);
        pm.registerClass(Tool.class, MergePlaceTool.class);
        pm.registerClass(Tool.class, TransitionMergerTool.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.petri.PetriNetModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.petri.PetriNetDescriptor\"/>");
    }

}
