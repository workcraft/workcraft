package org.workcraft.plugins.graph;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.plugins.graph.commands.GraphReachabilityVerificationCommand;
import org.workcraft.plugins.graph.commands.GraphToPetriConversionCommand;
import org.workcraft.plugins.graph.serialisation.VertexDeserialiser;
import org.workcraft.plugins.graph.serialisation.VertexSerialiser;

public class GraphModule implements Module {

    @Override
    public String getDescription() {
        return "Directed Graphs";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerModel(GraphDescriptor.class);

        pm.registerXmlSerialiser(VertexSerialiser.class);
        pm.registerXmlDeserialiser(VertexDeserialiser.class);

        ScriptableCommandUtils.register(GraphToPetriConversionCommand.class, "convertGraphToPetri",
                "convert the given Graph 'work' into a new Petri net work");
        ScriptableCommandUtils.register(GraphReachabilityVerificationCommand.class, "checkGraphReachability",
                "check the Graph 'work' for reachability of all its nodes");
    }

}
