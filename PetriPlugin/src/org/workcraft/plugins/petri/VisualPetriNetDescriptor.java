package org.workcraft.plugins.petri;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

public class VisualPetriNetDescriptor implements VisualModelDescriptor {

    @Override
    public VisualPetriNet create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, PetriNet.class, VisualPetriNet.class.getSimpleName());
        return new VisualPetriNet((PetriNet) mathModel);
    }

}
