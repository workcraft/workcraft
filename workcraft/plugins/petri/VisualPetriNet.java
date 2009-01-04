package org.workcraft.plugins.petri;

import java.lang.reflect.Array;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.VisualModelConstructionException;

public class VisualPetriNet extends VisualModel {
	@Override
	public Class<?>[] getAdditionalToolClasses() {
		return new Class<?>[]
		                    { SimulationTool.class };
	}

	public VisualPetriNet(PetriNet model)
	throws VisualModelConstructionException {
		super(model);
	}

	public VisualPetriNet(PetriNet model, Element visualElement) throws VisualModelConstructionException {
		super(model, visualElement);

	}
}