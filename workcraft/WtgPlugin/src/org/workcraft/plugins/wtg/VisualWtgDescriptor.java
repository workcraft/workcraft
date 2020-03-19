package org.workcraft.plugins.wtg;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

public class VisualWtgDescriptor implements VisualModelDescriptor {

    @Override
    public VisualWtg create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Wtg.class, VisualWtg.class.getSimpleName());
        return new VisualWtg((Wtg) mathModel);
    }

}
