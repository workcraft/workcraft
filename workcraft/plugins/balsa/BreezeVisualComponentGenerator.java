package org.workcraft.plugins.balsa;

import org.workcraft.dom.Component;
import org.workcraft.dom.VisualComponentGenerator;
import org.workcraft.dom.visual.VisualNode;

public class BreezeVisualComponentGenerator extends VisualComponentGenerator {

	public VisualNode createComponent(Component component) {
		return new VisualBreezeComponent((BreezeComponent)component);
	}

}
