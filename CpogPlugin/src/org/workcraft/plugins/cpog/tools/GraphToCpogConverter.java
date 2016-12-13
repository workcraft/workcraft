package org.workcraft.plugins.cpog.tools;

import java.util.Map;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.DefaultModelConverter;
import org.workcraft.plugins.cpog.Vertex;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.graph.VisualGraph;

public class GraphToCpogConverter extends DefaultModelConverter<VisualGraph, VisualCpog> {

    public GraphToCpogConverter(VisualGraph srcModel, VisualCpog dstModel) {
        super(srcModel, dstModel);
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
