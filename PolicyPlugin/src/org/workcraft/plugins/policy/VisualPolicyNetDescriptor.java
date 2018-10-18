package org.workcraft.plugins.policy;

import org.workcraft.util.ValidationUtils;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;

public class VisualPolicyNetDescriptor implements VisualModelDescriptor {

    @Override
    public VisualModel create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, PolicyNet.class, VisualPolicyNet.class.getSimpleName());
        return new VisualPolicyNet((PolicyNet) mathModel);
    }

}
