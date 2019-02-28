package org.workcraft.plugins.dfs;

import org.workcraft.utils.ValidationUtils;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;

public class VisualDfsDescriptor implements VisualModelDescriptor {

    @Override
    public VisualDfs create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Dfs.class, VisualDfs.class.getSimpleName());
        return new VisualDfs((Dfs) mathModel);
    }

}
