package org.workcraft.plugins.graph;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.plugins.graph.commands.ReachabilityVerificationCommand;
import org.workcraft.plugins.graph.commands.GraphToPetriConversionCommand;
import org.workcraft.plugins.graph.serialisation.VertexDeserialiser;
import org.workcraft.plugins.graph.serialisation.VertexSerialiser;

@SuppressWarnings("unused")
public class GraphPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Directed Graphs plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerModelDescriptor(GraphDescriptor.class);

        pm.registerXmlSerialiser(VertexSerialiser.class);
        pm.registerXmlDeserialiser(VertexDeserialiser.class);

        ScriptableCommandUtils.registerCommand(GraphToPetriConversionCommand.class, "convertGraphToPetri",
                "convert the Graph 'work' into a new Petri net work");
        ScriptableCommandUtils.registerCommand(ReachabilityVerificationCommand.class, "checkGraphReachability",
                "check the Graph 'work' for reachability of all its nodes");
    }

}
