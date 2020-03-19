package org.workcraft.plugins.fst;

import org.workcraft.utils.ValidationUtils;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;

public class VisualFstDescriptor implements VisualModelDescriptor {

    @Override
    public VisualFst create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Fst.class, VisualFst.class.getSimpleName());
        return new VisualFst((Fst) mathModel);
    }

}
