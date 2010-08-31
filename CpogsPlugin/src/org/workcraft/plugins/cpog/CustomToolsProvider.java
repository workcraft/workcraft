package org.workcraft.plugins.cpog;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class CustomToolsProvider implements
		org.workcraft.gui.graph.tools.CustomToolsProvider {

	@Override
	public Iterable<GraphEditorTool> getTools()
	{
		ArrayList<GraphEditorTool> res = new ArrayList<GraphEditorTool>();

		res.add(new SelectionTool());
		res.add(new ConnectionTool());

		return res;
	}

}
