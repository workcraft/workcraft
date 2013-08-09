package org.workcraft.plugins.sdfs;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.sdfs.tools.SimulationTool;

public class SDFSToolsProvider implements CustomToolsProvider {

	@Override
	public Iterable<GraphEditorTool> getTools() {
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();

		result.add(new SelectionTool());
		result.add(new CommentGeneratorTool());
		result.add(new ConnectionTool());
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Logic.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Register.class)));
		result.add(new SimulationTool());
		return result;
	}

}
