package org.workcraft.plugins.policy;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

public class VisualPolicyDescriptor implements VisualModelDescriptor {

    @Override
    public VisualPolicy create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Policy.class, VisualPolicy.class.getSimpleName());
        return new VisualPolicy((Policy) mathModel);
    }

}
