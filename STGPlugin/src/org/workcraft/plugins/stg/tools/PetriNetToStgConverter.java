package org.workcraft.plugins.stg.tools;

import java.util.Map;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.tools.DefaultModelConverter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class PetriNetToStgConverter extends DefaultModelConverter<VisualPetriNet, VisualSTG> {

	public PetriNetToStgConverter(VisualPetriNet srcModel, VisualSTG dstModel) {
		super(srcModel, dstModel);
	}

	@Override
	public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
		Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
		result.put(Place.class, STGPlace.class);
		result.put(Transition.class, DummyTransition.class);
		return result;
	}

	@Override
	public VisualComponent convertComponent(VisualComponent srcComponent) {
		VisualComponent dstComponent = super.convertComponent(srcComponent);
		if ( (dstComponent instanceof VisualDummyTransition) || (dstComponent instanceof VisualSignalTransition) ) {
			dstComponent.setLabel("");
		}
		return dstComponent;
	}

}
