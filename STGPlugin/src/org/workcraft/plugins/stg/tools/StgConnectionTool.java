package org.workcraft.plugins.stg.tools;

import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualNamedTransition;

public class StgConnectionTool extends ConnectionTool {

	public StgConnectionTool() {
		super(true, true);
	}

	@Override
	public boolean isConnectable(Node node) {
		return ( (node instanceof VisualPlace)
			  || (node instanceof VisualNamedTransition)
			  || (node instanceof VisualImplicitPlaceArc));
	}

}
