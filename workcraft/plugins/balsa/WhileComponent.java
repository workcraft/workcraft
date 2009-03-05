package org.workcraft.plugins.balsa;

import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.balsa.components.While;

@VisualClass("org.workcraft.plugins.balsa.VisualBreezeComponent")
public class WhileComponent extends BreezeComponent {

	public WhileComponent() {
		setUnderlyingComponent(new While());
	}
}
