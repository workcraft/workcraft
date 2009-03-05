package org.workcraft.plugins.balsa;

import org.workcraft.dom.Component;

public class BreezeComponent extends Component {
	private org.workcraft.plugins.balsa.components.Component underlyingComponent;
	public void setUnderlyingComponent(org.workcraft.plugins.balsa.components.Component underlyingComponent) {
		this.underlyingComponent = underlyingComponent;
	}

	public org.workcraft.plugins.balsa.components.Component getUnderlyingComponent() {
		return underlyingComponent;
	}
}
