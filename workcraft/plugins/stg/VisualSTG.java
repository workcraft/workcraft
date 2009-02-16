package org.workcraft.plugins.stg;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualConnection;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;

public class VisualSTG extends VisualPetriNet {

	@Override
	public void validateConnection(VisualComponent first, VisualComponent second)
			throws InvalidConnectionException {
		if (first instanceof VisualPlace)
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Connections between places are not allowed");

	}

	@Override
	public VisualConnection connect(VisualComponent first,
			VisualComponent second) throws InvalidConnectionException {
		return super.connect(first, second);
	}

	public VisualSTG(PetriNet model) throws VisualModelInstantiationException {
		super(model);
	}

	public VisualSTG(PetriNet model, Element element) throws VisualModelInstantiationException {
		super(model, element);
	}

}
