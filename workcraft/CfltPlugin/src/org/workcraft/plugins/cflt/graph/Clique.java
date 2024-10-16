package org.workcraft.plugins.cflt.graph;

import java.util.ArrayList;
import java.util.List;

public class Clique {
    List<String> vertexNames = new ArrayList<>();
    List<String> edgeNames = new ArrayList<>();

    public List<String> getVertexNames() {
        return vertexNames;
    }

    public List<String> getEdgeNames() {
        return edgeNames;
    }

    public void addEdgeName(String edgeName) {
        edgeNames.add(edgeName);
    }

    public void addVertexName(String vertexName) {
        vertexNames.add(vertexName);
    }
}
