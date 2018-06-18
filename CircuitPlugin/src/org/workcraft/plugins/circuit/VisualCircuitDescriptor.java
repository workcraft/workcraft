package org.workcraft.plugins.circuit;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.util.Hierarchy;

public class VisualCircuitDescriptor implements VisualModelDescriptor {

    @Override
    public VisualModel create(MathModel mathModel) throws VisualModelInstantiationException {
        VisualCircuit result = new VisualCircuit((Circuit) mathModel);
        for (VisualFunctionComponent component: Hierarchy.getDescendantsOfType(result.getRoot(), VisualFunctionComponent.class)) {
            if (component.getIsEnvironment()) {
                component.setRenderType(RenderType.BOX);
            } else {
                component.setRenderType(RenderType.GATE);
            }
        }
        return result;
    }

}
