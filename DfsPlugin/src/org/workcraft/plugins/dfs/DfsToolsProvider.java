package org.workcraft.plugins.dfs;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.dfs.tools.CycleAnalyserTool;
import org.workcraft.plugins.dfs.tools.DfsSimulationTool;

public class DfsToolsProvider implements CustomToolsProvider {

    @Override
    public Iterable<GraphEditorTool> getTools() {
        ArrayList<GraphEditorTool> result = new ArrayList<>();

        result.add(new SelectionTool(false));
        result.add(new CommentGeneratorTool());
        result.add(new ConnectionTool());
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Logic.class)));
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Register.class)));
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(ControlRegister.class)));
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(PushRegister.class)));
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(PopRegister.class)));
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(CounterflowLogic.class)));
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(CounterflowRegister.class)));
        result.add(new CycleAnalyserTool());
        result.add(new DfsSimulationTool());
        return result;
    }

}
