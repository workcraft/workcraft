package org.workcraft.plugins.stg;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.stg.tools.STGSelectionTool;
import org.workcraft.plugins.stg.tools.STGSignalTransitionGeneratorTool;
import org.workcraft.plugins.stg.tools.STGSimulationTool;

public class STGToolsProvider implements CustomToolsProvider {

	@Override
	public Iterable<GraphEditorTool> getTools() {
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();

		result.add(new STGSelectionTool());
		result.add(new ConnectionTool());
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(STGPlace.class)));
		result.add(new STGSignalTransitionGeneratorTool());
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(DummyTransition.class)));
		result.add(new STGSimulationTool());
		return result;
	}

}
