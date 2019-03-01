package org.workcraft.plugins.graph;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

public class VisualGraphDescriptor implements VisualModelDescriptor {

    @Override
    public VisualGraph create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Graph.class, VisualGraph.class.getSimpleName());
        return new VisualGraph((Graph) mathModel);
    }

}
