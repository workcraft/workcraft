package org.workcraft.plugins.cpog;

import org.workcraft.utils.ValidationUtils;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;

public class VisualCpogDescriptor implements VisualModelDescriptor {

    @Override
    public VisualCpog create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Cpog.class, VisualCpog.class.getSimpleName());
        return new VisualCpog((Cpog) mathModel);
    }

}
