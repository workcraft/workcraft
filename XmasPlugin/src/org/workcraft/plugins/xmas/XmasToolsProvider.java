package org.workcraft.plugins.xmas;


import java.util.ArrayList;

import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.xmas.components.ForkComponent;
import org.workcraft.plugins.xmas.components.FunctionComponent;
import org.workcraft.plugins.xmas.components.JoinComponent;
import org.workcraft.plugins.xmas.components.MergeComponent;
import org.workcraft.plugins.xmas.components.QueueComponent;
import org.workcraft.plugins.xmas.components.SinkComponent;
import org.workcraft.plugins.xmas.components.SourceComponent;
import org.workcraft.plugins.xmas.components.SwitchComponent;

public class XmasToolsProvider implements CustomToolsProvider {

	@Override
	public Iterable<GraphEditorTool> getTools() {
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();

		result.add(new SelectionTool());
		result.add(new CommentGeneratorTool());
		result.add(new ConnectionTool());

		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(SourceComponent.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(SinkComponent.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(FunctionComponent.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(QueueComponent.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(ForkComponent.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(JoinComponent.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(SwitchComponent.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(MergeComponent.class)));

		return result;
	}

}
