package org.workcraft.plugins.balsa;

import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.balsa.components.Adapt;

@VisualClass("org.workcraft.plugins.balsa.VisualBreezeComponent")
public class AdaptComponent extends BreezeComponent {
	public AdaptComponent() {
		setUnderlyingComponent(new Adapt());
	}
}
