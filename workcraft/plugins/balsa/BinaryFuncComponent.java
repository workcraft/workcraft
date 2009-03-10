package org.workcraft.plugins.balsa;

import org.w3c.dom.Element;
import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.balsa.components.BinaryFunc;

@VisualClass("org.workcraft.plugins.balsa.VisualBreezeComponent")
public class BinaryFuncComponent extends BreezeComponent {

	public BinaryFuncComponent(Element e) {
		super(e);
	}

	public BinaryFuncComponent() {
		BinaryFunc func = new BinaryFunc();
		func.setInputAWidth(10);
		func.setInputBWidth(10);
		func.setOutputWidth(10);
		setUnderlyingComponent(func);
	}
}
