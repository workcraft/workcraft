package org.workcraft.dom.visual.connections;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditorMouseListener;

public class DefaultAnchorGenerator implements GraphEditorMouseListener {
	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		if (e.getClickCount()==2) {
			Node n = HitMan.hitTestForSelection(e.getPosition(), e.getModel());
			if (n instanceof VisualConnection) {
				VisualConnection con = (VisualConnection)n;
				if (con.getGraphic() instanceof Polyline)
					((Polyline)con.getGraphic()).addAnchorPoint(e.getPosition());
			}
		}
	}

	@Override
	public void mouseEntered(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseExited(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseReleased(GraphEditorMouseEvent e) {
	}
}