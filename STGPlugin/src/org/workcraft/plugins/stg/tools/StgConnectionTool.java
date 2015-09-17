package org.workcraft.plugins.stg.tools;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.tools.PetriNetConnectionTool;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;

public class StgConnectionTool extends PetriNetConnectionTool {

	@Override
	public boolean isConnectable(Node node) {
		return (super.isConnectable(node)
			|| (node instanceof VisualImplicitPlaceArc));
	}

}
