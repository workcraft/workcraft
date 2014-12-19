package org.workcraft.plugins.stg.tools;

import java.util.Map;

import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.graph.tools.DefaultModelConverter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.plugins.stg.VisualSTG;

public class StgToPetriNetConverter extends DefaultModelConverter<VisualSTG, VisualPetriNet>  {

	public StgToPetriNetConverter(VisualSTG srcModel, VisualPetriNet dstModel) {
		super(srcModel, dstModel);
	}

	@Override
	public Map<Class<? extends MathNode>, Class<? extends MathNode>> getClassMap() {
		Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getClassMap();
		result.put(STGPlace.class, Place.class);
		result.put(DummyTransition.class, Transition.class);
		return result;
	}

}
