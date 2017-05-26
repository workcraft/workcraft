package org.workcraft.plugins.policy;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.plugins.petri.tools.PetriPlaceGeneratorTool;
import org.workcraft.plugins.policy.tools.PolicyBundledTransitionGeneratorTool;
import org.workcraft.plugins.policy.tools.PolicySelectionTool;
import org.workcraft.plugins.policy.tools.PolicySimulationTool;

public class PolicyNetToolProvider implements CustomToolsProvider {

    @Override
    public Iterable<GraphEditorTool> getTools() {
        ArrayList<GraphEditorTool> result = new ArrayList<>();

        result.add(new PolicySelectionTool());
        result.add(new CommentGeneratorTool());
        result.add(new ConnectionTool());

        result.add(new PetriPlaceGeneratorTool());
        result.add(new PolicyBundledTransitionGeneratorTool());
        result.add(new PolicySimulationTool());

        return result;
    }

}
