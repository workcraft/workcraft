package org.workcraft.plugins.fsm.tools;

import java.util.Map;

import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.graph.tools.DefaultModelConverter;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.plugins.graph.VisualGraph;

public class FsmToGraphConverter extends DefaultModelConverter<VisualFsm, VisualGraph> {

    public FsmToGraphConverter(VisualFsm srcModel, VisualGraph dstModel) {
        super(srcModel, dstModel);
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(State.class, Vertex.class);
        return result;
    }

}
