package org.workcraft.dom;

import org.workcraft.dom.visual.VisualNode;

public interface VisualComponentGenerator {
	public VisualNode createComponent(Component component, Object ... constructorParameters);

}
