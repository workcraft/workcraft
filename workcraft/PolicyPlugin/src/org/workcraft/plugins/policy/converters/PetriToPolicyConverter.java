package org.workcraft.plugins.policy.converters;

import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.converters.DefaultPetriConverter;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.plugins.policy.Policy;
import org.workcraft.plugins.policy.VisualPolicy;

import java.util.Map;

public class PetriToPolicyConverter extends DefaultPetriConverter<VisualPetri, VisualPolicy> {

    public PetriToPolicyConverter(VisualPetri srcModel) {
        super(srcModel, new VisualPolicy(new Policy()));
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(Place.class, Place.class);
        result.put(Transition.class, BundledTransition.class);
        return result;
    }

}
