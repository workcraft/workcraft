package org.workcraft.plugins.layout;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.layout.Layout;

@DisplayName ("Zero")
public class NullLayout implements Layout {
	public void doLayout(VisualModel model) {
		for (VisualNode n : model.getRoot().getChildren()) {
			if (n instanceof VisualTransformableNode) {
				((VisualTransformableNode)n).setX(0);
				((VisualTransformableNode)n).setY(0);
			}
		}
	}

	public boolean isApplicableTo(VisualModel model) {
			return true;
	}
}