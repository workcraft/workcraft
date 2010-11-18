package org.workcraft.plugins.circuit;


import java.util.ArrayList;

import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.plugins.circuit.tools.CheckCircuitTool;
import org.workcraft.plugins.circuit.tools.CircuitSelectionTool;
import org.workcraft.plugins.circuit.tools.CircuitSimulationTool;
import org.workcraft.plugins.circuit.tools.ContactGeneratorTool;

public class CircuitToolsProvider implements CustomToolsProvider {

	@Override
	public Iterable<GraphEditorTool> getTools() {
		ArrayList<GraphEditorTool> result = new ArrayList<GraphEditorTool>();

		result.add(new CircuitSelectionTool());
		result.add(new ConnectionTool());
		result.add(new CircuitSimulationTool());
		result.add(new ContactGeneratorTool());

		return result;
	}

}
