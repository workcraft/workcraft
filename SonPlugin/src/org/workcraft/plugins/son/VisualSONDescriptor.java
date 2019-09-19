package org.workcraft.plugins.son;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

public class VisualSONDescriptor implements VisualModelDescriptor {

    @Override
    public VisualSON create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, SON.class, VisualSON.class.getSimpleName());
        return new VisualSON((SON) mathModel);
    }

}
