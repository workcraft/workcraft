package org.workcraft.plugins.policy.tools;

import java.util.Map;

import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.graph.tools.DefaultModelConverter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;

public 	class PetriNetToPolicyNetConverter extends DefaultModelConverter<VisualPetriNet, VisualPolicyNet>  {

	public PetriNetToPolicyNetConverter(VisualPetriNet srcModel, VisualPolicyNet dstModel) {
		super(srcModel, dstModel);
	}

	@Override
	public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
		Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
		result.put(Place.class, Place.class);
		result.put(Transition.class, BundledTransition.class);
		return result;
	}

}
