package org.workcraft.plugins.balsa;

import org.workcraft.dom.Component;
import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.balsa.components.IHandshakedStgComponent;
import org.workcraft.plugins.balsa.components.While;

@VisualClass("org.workcraft.plugins.balsa.VisualBreezeComponent")
public class BreezeComponent extends Component {
	While underlyingComponent;

	IHandshakedStgComponent getUnderlyingComponent()
	{
		return underlyingComponent;
	}

	public BreezeComponent() {
		underlyingComponent = new While();
	}
}
