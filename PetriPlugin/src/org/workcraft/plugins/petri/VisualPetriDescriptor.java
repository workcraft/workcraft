package org.workcraft.plugins.petri;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

public class VisualPetriDescriptor implements VisualModelDescriptor {

    @Override
    public VisualPetri create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Petri.class, VisualPetri.class.getSimpleName());
        return new VisualPetri((Petri) mathModel);
    }

}
