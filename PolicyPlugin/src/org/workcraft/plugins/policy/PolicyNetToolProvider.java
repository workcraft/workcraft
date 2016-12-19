package org.workcraft.plugins.policy;

import java.util.ArrayList;

import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.policy.tools.PolicySelectionTool;
import org.workcraft.plugins.policy.tools.PolicySimulationTool;

public class PolicyNetToolProvider implements CustomToolsProvider {

    @Override
    public Iterable<GraphEditorTool> getTools() {
        ArrayList<GraphEditorTool> result = new ArrayList<>();

        result.add(new PolicySelectionTool());
        result.add(new CommentGeneratorTool());
        result.add(new ConnectionTool());

        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Place.class)));
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(BundledTransition.class)));
        result.add(new PolicySimulationTool());

        return result;
    }

}
