package org.workcraft.plugins.fsm;

import java.util.ArrayList;

import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.fsm.tools.FsmSimulationTool;

public class FsmToolsProvider implements CustomToolsProvider {

    @Override
    public Iterable<GraphEditorTool> getTools() {
        ArrayList<GraphEditorTool> result = new ArrayList<>();

        result.add(new SelectionTool(true, false, true, true));
        result.add(new CommentGeneratorTool());
        result.add(new ConnectionTool(false, true, true, true));
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(State.class)));
        result.add(new FsmSimulationTool());
        return result;
    }
}
