package org.workcraft.plugins.fsm.converters;

import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.plugins.graph.VisualGraph;

import java.util.Map;

public class FsmToGraphConverter extends DefaultModelConverter<VisualFsm, VisualGraph> {

    public FsmToGraphConverter(VisualFsm srcModel) {
        super(srcModel, new VisualGraph(new Graph()));
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(State.class, Vertex.class);
        return result;
    }

}
