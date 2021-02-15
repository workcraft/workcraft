package org.workcraft.plugins.fsm.converters;

import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.plugins.graph.VisualGraph;

import java.util.Map;

public class GraphToFsmConverter extends DefaultModelConverter<VisualGraph, VisualFsm> {

    public GraphToFsmConverter(VisualGraph srcModel) {
        super(srcModel, new VisualFsm(new Fsm()));
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(Vertex.class, State.class);
        return result;
    }

}
