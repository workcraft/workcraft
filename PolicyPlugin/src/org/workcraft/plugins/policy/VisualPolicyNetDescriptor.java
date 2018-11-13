package org.workcraft.plugins.policy;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.util.ValidationUtils;

public class VisualPolicyNetDescriptor implements VisualModelDescriptor {

    @Override
    public VisualPolicyNet create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, PolicyNet.class, VisualPolicyNet.class.getSimpleName());
        return new VisualPolicyNet((PolicyNet) mathModel);
    }

}
