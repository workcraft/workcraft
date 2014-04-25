package org.workcraft.plugins.son;

import java.util.ArrayList;

import org.workcraft.dom.math.PageNode;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.PageGeneratorTool;
import org.workcraft.plugins.son.components.ChannelPlace;
import org.workcraft.plugins.son.components.Condition;
import org.workcraft.plugins.son.components.Event;
import org.workcraft.plugins.son.tools.SONConnectionTool;
import org.workcraft.plugins.son.tools.SONSimulationTool;
import org.workcraft.plugins.son.tools.SelectionTool;

public class SONToolProvider implements CustomToolsProvider{

	@Override
	public Iterable<GraphEditorTool> getTools() {
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();
		GraphEditorTool channelPlaceTool = new NodeGeneratorTool(new DefaultNodeGenerator(ChannelPlace.class));
		result.add(new SelectionTool(channelPlaceTool));
		result.add(new PageGeneratorTool(new DefaultNodeGenerator(PageNode.class)));
		result.add(new CommentGeneratorTool());
		result.add(new SONConnectionTool());
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Condition.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Event.class)));
		result.add(channelPlaceTool);
		result.add(new SONSimulationTool());

		return result;
	}
}
