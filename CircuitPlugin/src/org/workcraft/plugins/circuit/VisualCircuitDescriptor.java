package org.workcraft.plugins.circuit;

import org.workcraft.utils.ValidationUtils;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.utils.Hierarchy;

public class VisualCircuitDescriptor implements VisualModelDescriptor {

    @Override
    public VisualCircuit create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Circuit.class, VisualCircuit.class.getSimpleName());

        VisualCircuit result = new VisualCircuit((Circuit) mathModel);
        for (VisualFunctionComponent component : Hierarchy.getDescendantsOfType(result.getRoot(), VisualFunctionComponent.class)) {
            if (component.getIsEnvironment()) {
                component.setRenderType(RenderType.BOX);
            } else {
                component.setRenderType(RenderType.GATE);
            }
        }
        return result;
    }

}
