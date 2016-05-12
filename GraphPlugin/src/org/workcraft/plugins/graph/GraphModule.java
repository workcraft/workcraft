package org.workcraft.plugins.graph;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.plugins.graph.serialisation.VertexDeserialiser;
import org.workcraft.plugins.graph.serialisation.VertexSerialiser;
import org.workcraft.plugins.graph.tools.GraphToPetriConverterTool;
import org.workcraft.plugins.graph.tools.ReachabilityCheckerTool;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class GraphModule implements Module {

    @Override
    public String getDescription() {
        return "Directed Graphs";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(ModelDescriptor.class, GraphDescriptor.class);

        pm.registerClass(XMLSerialiser.class, VertexSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, VertexDeserialiser.class);

        pm.registerClass(Tool.class, GraphToPetriConverterTool.class);
        pm.registerClass(Tool.class, ReachabilityCheckerTool.class);
    }

}
