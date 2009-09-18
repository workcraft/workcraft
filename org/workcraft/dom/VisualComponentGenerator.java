package org.workcraft.dom;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualNode;

public interface VisualComponentGenerator {
	public VisualNode createComponent(MathNode component, Object ... constructorParameters);

}