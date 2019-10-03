package org.workcraft.plugins.xbm;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

public class VisualXbmDescriptor implements VisualModelDescriptor {

    @Override
    public VisualModel create(MathModel mathModel) throws VisualModelInstantiationException {

        ValidationUtils.validateMathModelType(mathModel, Xbm.class, VisualXbm.class.getSimpleName());
        return new VisualXbm((Xbm) mathModel);
    }
}
