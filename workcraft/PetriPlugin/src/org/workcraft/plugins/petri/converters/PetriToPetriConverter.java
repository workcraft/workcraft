package org.workcraft.plugins.petri.converters;

import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetri;

import java.util.Map;

public class PetriToPetriConverter extends DefaultPetriConverter<VisualPetri, VisualPetri> {

    public PetriToPetriConverter(VisualPetri srcModel) {
        super(srcModel, new VisualPetri(new Petri()));
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(Place.class, Place.class);
        result.put(Transition.class, Transition.class);
        return result;
    }

}
