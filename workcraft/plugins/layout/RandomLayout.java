package org.workcraft.plugins.layout;

import java.util.Random;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.layout.Layout;

public class RandomLayout implements Layout {
	public String getDisplayName() {
		return "Random layout";
	}
	Random r = new Random();
	public void doLayout(VisualModel model) {
		for (VisualNode n : model.getRoot().getChildren()) {
			if (n instanceof VisualTransformableNode) {
				((VisualTransformableNode)n).setX(r.nextDouble()*30);
				((VisualTransformableNode)n).setY(r.nextDouble()*30);
			}
		}
	}

	public boolean isApplicableTo(VisualModel model) {
			return true;
	}
}