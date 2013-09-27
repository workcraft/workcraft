package org.workcraft.plugins.cpog;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.plugins.cpog.expressions.CpogExpressionTool;

public class CustomToolsProvider implements
		org.workcraft.gui.graph.tools.CustomToolsProvider {

	@Override
	public Iterable<GraphEditorTool> getTools()
	{
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();

		result.add(new SelectionTool());
		result.add(new CommentGeneratorTool());
		result.add(new ConnectionTool());
		result.add(new CpogExpressionTool());

		return result;
	}

}
