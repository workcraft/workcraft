package org.workcraft.plugins.balsa;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;

public final class VisualBalsaCircuit extends VisualModel {

	public VisualBalsaCircuit(BalsaCircuit model) throws VisualModelInstantiationException {
		super(model);
	}

	public VisualBalsaCircuit(BalsaCircuit model, Element element) throws VisualModelInstantiationException {
		super(model, element);
	}

}
