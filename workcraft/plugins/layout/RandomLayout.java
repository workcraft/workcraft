package org.workcraft.plugins.layout;

import java.util.Random;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.layout.Layout;

@DisplayName("Random")
public class RandomLayout implements Layout {
	Random r = new Random();
	public void doLayout(VisualModel model) {
		for (Node n : model.getRoot().getChildren()) {
			if (n instanceof VisualTransformableNode) {
				((VisualTransformableNode)n).setX(RandomLayoutSettings.startX + r.nextDouble()*RandomLayoutSettings.rangeX);
				((VisualTransformableNode)n).setY(RandomLayoutSettings.startY + r.nextDouble()*RandomLayoutSettings.rangeY);
			}
		}
	}

	public boolean isApplicableTo(VisualModel model) {
			return true;
	}
}