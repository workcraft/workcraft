package org.workcraft.plugins.circuit;


import java.util.ArrayList;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.circuit.tools.CircuitSelectionTool;
import org.workcraft.plugins.circuit.tools.CircuitSimulationTool;
import org.workcraft.plugins.circuit.tools.ContactGeneratorTool;
import org.workcraft.plugins.circuit.tools.FunctionComponentGeneratorTool;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;

public class CircuitToolsProvider implements CustomToolsProvider {

	@Override
	public Iterable<GraphEditorTool> getTools() {
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();

		result.add(new CircuitSelectionTool());
		result.add(new CommentGeneratorTool());
		result.add(new ConnectionTool() {
			@Override
			protected boolean isConnectable(Node node) {
				return true;
			}
		});
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Joint.class)));
		result.add(new FunctionComponentGeneratorTool());
		result.add(new ContactGeneratorTool());
		result.add(new CircuitSimulationTool());

		return result;
	}

}
