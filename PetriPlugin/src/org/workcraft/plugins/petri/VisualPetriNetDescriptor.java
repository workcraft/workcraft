package org.workcraft.plugins.petri;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;

public class VisualPetriNetDescriptor implements VisualModelDescriptor {

    @Override
    public VisualModel create(MathModel mathModel) {
        return new VisualPetriNet((PetriNet) mathModel);
    }

}
