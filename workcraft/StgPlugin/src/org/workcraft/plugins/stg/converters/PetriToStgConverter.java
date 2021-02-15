package org.workcraft.plugins.stg.converters;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.converters.DefaultPetriConverter;
import org.workcraft.plugins.stg.*;

import java.util.Map;

public class PetriToStgConverter extends DefaultPetriConverter<VisualPetri, VisualStg> {

    public PetriToStgConverter(VisualPetri srcModel) {
        super(srcModel, new VisualStg(new Stg()));
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(Place.class, StgPlace.class);
        result.put(Transition.class, DummyTransition.class);
        return result;
    }

    @Override
    public VisualComponent convertComponent(VisualComponent srcComponent) {
        VisualComponent dstComponent = super.convertComponent(srcComponent);
        if ((dstComponent instanceof VisualDummyTransition) || (dstComponent instanceof VisualSignalTransition)) {
            dstComponent.setLabel("");
        }
        return dstComponent;
    }

}
