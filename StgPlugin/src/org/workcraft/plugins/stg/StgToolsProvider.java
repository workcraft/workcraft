package org.workcraft.plugins.stg;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.plugins.petri.tools.ReadArcConnectionTool;
import org.workcraft.plugins.stg.tools.EncodingConflictAnalyserTool;
import org.workcraft.plugins.stg.tools.StgConnectionTool;
import org.workcraft.plugins.stg.tools.StgDummyTransitionGeneratorTool;
import org.workcraft.plugins.stg.tools.StgPlaceGeneratorTool;
import org.workcraft.plugins.stg.tools.StgSelectionTool;
import org.workcraft.plugins.stg.tools.StgSignalTransitionGeneratorTool;
import org.workcraft.plugins.stg.tools.StgSimulationTool;

public class StgToolsProvider implements CustomToolsProvider {

    @Override
    public Iterable<GraphEditorTool> getTools() {
        ArrayList<GraphEditorTool> result = new ArrayList<>();

        result.add(new StgSelectionTool());
        result.add(new CommentGeneratorTool());
        result.add(new StgConnectionTool());
        result.add(new ReadArcConnectionTool());
        result.add(new StgPlaceGeneratorTool());
        result.add(new StgSignalTransitionGeneratorTool());
        result.add(new StgDummyTransitionGeneratorTool());
        result.add(new StgSimulationTool());
        result.add(new EncodingConflictAnalyserTool());
        return result;
    }

}
