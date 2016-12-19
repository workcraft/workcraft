package org.workcraft.plugins.dtd;

import java.util.ArrayList;

import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.dtd.tools.DtdSelectionTool;

public class DtdToolsProvider implements CustomToolsProvider {

    @Override
    public Iterable<GraphEditorTool> getTools() {
        ArrayList<GraphEditorTool> result = new ArrayList<>();

        result.add(new DtdSelectionTool());
        result.add(new CommentGeneratorTool());
        result.add(new ConnectionTool(false, true, false));
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Signal.class)));
        return result;
    }

}
