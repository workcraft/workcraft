package org.workcraft.plugins.cflt;

import java.util.ArrayList;
import java.util.List;

public class Clique {
    List<String> vertexNames = new ArrayList<>();
    List<String> edgeNames = new ArrayList<>();

    public Clique(List<String> vertexNames, List<String> edgeNames) {
        this.vertexNames = vertexNames;
        this.edgeNames = edgeNames;
    }

    public Clique() {}

    public List<String> getVertexNames() {
        return vertexNames;
    }

    public void setVertexNames(List<String> vertexNames) {
        this.vertexNames = vertexNames;
    }

    public List<String> getEdgeNames() {
        return edgeNames;
    }

    public void setEdgeNames(List<String> edgeNames) {
        this.edgeNames = edgeNames;
    }

    public void addEdgeName(String edgeName) {
        edgeNames.add(edgeName);
    }

    public void removeEdgeName(String edgeName) {
        edgeNames.remove(edgeName);
    }

    public void addVertexName(String vertexName) {
        vertexNames.add(vertexName);
    }

    public void removeVertexName(String vertexName) {
        vertexNames.remove(vertexName);
    }
}
