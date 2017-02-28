package org.workcraft.plugins.dtd;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.plugins.dtd.tools.DtdConnectionTool;
import org.workcraft.plugins.dtd.tools.DtdSelectionTool;
import org.workcraft.plugins.dtd.tools.DtdSignalGeneratorTool;

public class DtdToolsProvider implements CustomToolsProvider {

    @Override
    public Iterable<GraphEditorTool> getTools() {
        ArrayList<GraphEditorTool> result = new ArrayList<>();

        result.add(new DtdSelectionTool());
        result.add(new CommentGeneratorTool());
        result.add(new DtdConnectionTool());
        result.add(new DtdSignalGeneratorTool());
        return result;
    }

}
