package org.workcraft.plugins.stg.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;

public class StgConnectionTool extends ConnectionTool {

	public StgConnectionTool(boolean forbidSelfLoops) {
		super(forbidSelfLoops);
	}

	@Override
	public boolean isConnectable(Node node) {
		return !(node instanceof VisualConnection) || (node instanceof VisualImplicitPlaceArc);
	}

}
