package org.workcraft.plugins.xmas.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.xmas.components.VisualXmasConnection;

public class XmasConnectionTool extends ConnectionTool {

	@Override
	public boolean isConnectable(Node node) {
		return true;
	}

	@Override
	public VisualConnection createDefaultTemplateNode() {
		VisualXmasConnection result = new VisualXmasConnection();
		result.setArrowLength(0.0);
		return result;
	}

}
