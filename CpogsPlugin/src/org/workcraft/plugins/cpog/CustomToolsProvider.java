package org.workcraft.plugins.cpog;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;

public class CustomToolsProvider implements
		org.workcraft.gui.graph.tools.CustomToolsProvider {

	@Override
	public Iterable<GraphEditorTool> getTools()
	{
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();

		result.add(new CpogSelectionTool());
		result.add(new CommentGeneratorTool());
		result.add(new ConnectionTool());

		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Vertex.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Variable.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(RhoClause.class)));


		return result;
	}

}
