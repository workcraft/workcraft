package org.workcraft.plugins.balsa;

import org.w3c.dom.Element;
import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.balsa.components.Adapt;

@VisualClass("org.workcraft.plugins.balsa.VisualBreezeComponent")
public class AdaptComponent extends BreezeComponent {
	public AdaptComponent() {
		setUnderlyingComponent(new Adapt());
	}
	public AdaptComponent(Element e) {
		super(e);
	}
}
