package org.workcraft.plugins.cflt;

public class Edge {

    private final String firstVertex;
    private final String secondVertex;

    public Edge(String vertex1, String vertex2) {
        this.firstVertex = vertex1;
        this.secondVertex = vertex2;
    }

    public String getFirstVertex() {
        return firstVertex;
    }

    public String getSecondVertex() {
        return secondVertex;
    }

}
