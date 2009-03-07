package org.workcraft.plugins.balsa;

import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.balsa.components.BinaryFunc;

@VisualClass("org.workcraft.plugins.balsa.VisualBreezeComponent")
public class BinaryFuncComponent extends BreezeComponent {

	public BinaryFuncComponent() {
		setUnderlyingComponent(new BinaryFunc());
	}
}
