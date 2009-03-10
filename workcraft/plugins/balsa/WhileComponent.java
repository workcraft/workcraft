package org.workcraft.plugins.balsa;

import org.w3c.dom.Element;
import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.balsa.components.While;

@VisualClass("org.workcraft.plugins.balsa.VisualBreezeComponent")
public class WhileComponent extends BreezeComponent {

	public WhileComponent(Element e) {
		super(e);
	}

	public WhileComponent() {
		setUnderlyingComponent(new While());
	}
}
