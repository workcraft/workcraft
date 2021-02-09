package org.workcraft.plugins.cpog.converters;

import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.cpog.Cpog;
import org.workcraft.plugins.cpog.Vertex;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.graph.VisualGraph;

import java.util.Map;

public class GraphToCpogConverter extends DefaultModelConverter<VisualGraph, VisualCpog> {

    public GraphToCpogConverter(VisualGraph srcModel) {
        super(srcModel, new VisualCpog(new Cpog()));
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(org.workcraft.plugins.graph.Vertex.class, Vertex.class);
        return result;
    }

    @Override
    public VisualConnection convertConnection(VisualConnection srcConnection) {
        VisualConnection dstConnection = null;
        if (srcConnection.getFirst() != srcConnection.getSecond()) {
            dstConnection = super.convertConnection(srcConnection);
        }
        return dstConnection;
    }

}
