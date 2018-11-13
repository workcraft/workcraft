package org.workcraft.plugins.stg;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.util.ValidationUtils;

public class VisualStgDescriptor implements VisualModelDescriptor {

    @Override
    public VisualStg create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Stg.class, VisualStg.class.getSimpleName());
        return new VisualStg((Stg) mathModel);
    }

}
