package org.workcraft.plugins.stg;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;

import org.w3c.dom.Element;
import org.workcraft.framework.plugins.HotKeyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;

@HotKeyDeclaration(KeyEvent.VK_T)
public class VisualSignalTransition extends VisualTransition {

	public VisualSignalTransition(Transition transition, Element xmlElement) {
		super(transition, xmlElement);
		addPropertyDeclarations();
	}

	public VisualSignalTransition(Transition transition) {
		super(transition);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		LinkedHashMap<String, Object> types = new LinkedHashMap<String, Object>();
		types.put("Input", SignalTransition.Type.INPUT);
		types.put("Output", SignalTransition.Type.OUTPUT);
		types.put("Internal", SignalTransition.Type.INTERNAL);
		types.put("Dummy", SignalTransition.Type.DUMMY);

		addPropertyDeclaration(new PropertyDeclaration("Type", "getType", "setType", SignalTransition.Type.class, types));
	}

	public SignalTransition.Type getType() {
		return ((SignalTransition)getReferencedComponent()).getType();
	}

	public void setType(SignalTransition.Type type) {
		((SignalTransition)getReferencedComponent()).setType(type);
	}
}
