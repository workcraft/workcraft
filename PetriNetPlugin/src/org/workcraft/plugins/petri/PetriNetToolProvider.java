package org.workcraft.plugins.petri;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.petri.tools.PetriNetSelectionTool;
import org.workcraft.plugins.petri.tools.PetriNetSimulationTool;
import org.workcraft.plugins.petri.tools.ReadArcConnectionTool;

public class PetriNetToolProvider implements CustomToolsProvider {

	@Override
	public Iterable<GraphEditorTool> getTools() {
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();

		result.add(new PetriNetSelectionTool());
		result.add(new CommentGeneratorTool());
		result.add(new ConnectionTool() {
			@Override
			public boolean isConnectable(Node node) {
				return ((node instanceof VisualPlace) || (node instanceof VisualTransition));
			}
		});
		result.add(new ReadArcConnectionTool());
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Place.class)));
		result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Transition.class)));
		result.add(new PetriNetSimulationTool());

		return result;
	}

}
