package org.workcraft.plugins.xmas;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.util.ValidationUtils;

public class VisualXmasDescriptor implements VisualModelDescriptor {

    @Override
    public VisualXmas create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Xmas.class, VisualXmas.class.getSimpleName());
        return new VisualXmas((Xmas) mathModel);
    }

}
