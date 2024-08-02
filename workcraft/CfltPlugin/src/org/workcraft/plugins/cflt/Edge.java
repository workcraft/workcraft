package org.workcraft.plugins.cflt;

public class Edge {

    private final String firstVertexName;
    private final String secondVertexName;

    public Edge(String vertex1, String vertex2) {
        this.firstVertexName = vertex1;
        this.secondVertexName = vertex2;
    }

    public String getFirstVertexName() {
        return firstVertexName;
    }

    public String getSecondVertexName() {
        return secondVertexName;
    }

}
