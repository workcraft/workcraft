package org.workcraft.plugins.fsm;

import org.workcraft.util.ValidationUtils;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;

public class VisualFsmDescriptor implements VisualModelDescriptor {

    @Override
    public VisualFsm create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Fsm.class, VisualFsm.class.getSimpleName());
        return new VisualFsm((Fsm) mathModel);
    }

}
