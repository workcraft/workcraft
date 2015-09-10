package org.workcraft.plugins.stg;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.petri.tools.ReadArcConnectionTool;
import org.workcraft.plugins.stg.tools.StgConnectionTool;
import org.workcraft.plugins.stg.tools.StgSelectionTool;
import org.workcraft.plugins.stg.tools.StgSignalTransitionGeneratorTool;
import org.workcraft.plugins.stg.tools.StgSimulationTool;

public class STGToolsProvider implements CustomToolsProvider {

	@Override
	public Iterable<GraphEditorTool> getTools() {
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();

		result.add(new StgSelectionTool());
		result.add(new CommentGeneratorTool());
		result.add(new StgConnectionTool());
		result.add(new ReadArcConnectionTool());
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(STGPlace.class)));
		result.add(new StgSignalTransitionGeneratorTool());
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(DummyTransition.class)));
		result.add(new StgSimulationTool());
		return result;
	}

}
