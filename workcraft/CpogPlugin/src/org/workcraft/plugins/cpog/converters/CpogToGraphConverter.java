package org.workcraft.plugins.cpog.converters;

import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.cpog.Vertex;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;

import java.util.Map;

public class CpogToGraphConverter extends DefaultModelConverter<VisualCpog, VisualGraph> {

    public CpogToGraphConverter(VisualCpog srcModel) {
        super(srcModel, new VisualGraph(new Graph()));
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(Vertex.class, org.workcraft.plugins.graph.Vertex.class);
        return result;
    }

}
