package org.workcraft.plugins.graph;

import org.workcraft.util.ValidationUtils;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;

public class VisualGraphDescriptor implements VisualModelDescriptor {

    @Override
    public VisualModel create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Graph.class, VisualGraph.class.getSimpleName());
        return new VisualGraph((Graph) mathModel);
    }

}
