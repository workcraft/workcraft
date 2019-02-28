package org.workcraft.plugins.cpog.converters;

import java.util.Map;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.plugins.cpog.Vertex;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.graph.VisualGraph;

public class CpogToGraphConverter extends DefaultModelConverter<VisualCpog, VisualGraph> {

    public CpogToGraphConverter(VisualCpog srcModel, VisualGraph dstModel) {
        super(srcModel, dstModel);
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(Vertex.class, org.workcraft.plugins.graph.Vertex.class);
        return result;
    }

}
