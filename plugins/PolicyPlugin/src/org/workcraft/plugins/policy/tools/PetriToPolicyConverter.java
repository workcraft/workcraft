package org.workcraft.plugins.policy.tools;

import java.util.Map;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.plugins.policy.VisualPolicy;

public class PetriToPolicyConverter extends DefaultModelConverter<VisualPetri, VisualPolicy> {

    public PetriToPolicyConverter(VisualPetri srcModel, VisualPolicy dstModel) {
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
