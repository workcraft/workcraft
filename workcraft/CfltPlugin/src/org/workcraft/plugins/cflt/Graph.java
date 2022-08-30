package org.workcraft.plugins.cflt;

import java.util.ArrayList;
import java.util.List;

/**
 * An undirected and unweighed graph as a set of edges
 */
public class Graph {

    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<String> vertices = new ArrayList<>();
    boolean[][] connections;

    public Graph(ArrayList<Edge> edgeList, ArrayList<String> vertices, boolean[][] connections) {
        this.setEdges(edgeList);
        this.connections = connections;
        this.vertices = vertices;
    }
    public Graph() {

    }
    public void initialiseConnections() {
        this.connections = new boolean[getVertices().size()][getVertices().size()];
        for (int x = 0; x < connections.length; x++) {
            for (int y = 0; y < connections.length; y++) {
                this.connections[x][y] = false;
            }
        }
        for (Edge e : getEdges()) {
            this.connections[getVertices().indexOf(e.getFirstVertex())][getVertices().indexOf(e.getSecondVertex())] = true;
            this.connections[getVertices().indexOf(e.getSecondVertex())][getVertices().indexOf(e.getFirstVertex())] = true;
        }
    }
    public void printGraph() {
        for (Edge edge : getEdges()) {
            System.out.println("Edge: " + edge.getFirstVertex() + " " + edge.getSecondVertex());
        }
        for (String s : getVertices()) {
            System.out.println("Vertex: " + s);
        }
    }
    public void addVertex(String v) {
        this.vertices.add(v);
    }
    public void removeVertex(String v) {
        this.vertices.remove(v);
    }
    public void addEdge(Edge e) {
        this.getEdges().add(e);
    }
    public void removeEdge(Edge e) {
        this.getEdges().remove(e);
    }
    public ArrayList<String> getIsolatedVertices() {

        if (getEdges().isEmpty()) {
            return getVertices();
        }
        @SuppressWarnings("unchecked")
        ArrayList<String> vertexList = (ArrayList<String>) vertices.clone();
        for (Edge e : getEdges()) {
            String v1 = e.getFirstVertex();
            String v2 = e.getSecondVertex();
            if (vertexList.contains(v1)) {
                vertexList.remove(v1);
            }
            if (vertexList.contains(v2)) {
                vertexList.remove(v2);
            }
        }
        return vertexList;

    }
    public void removeEdges(Graph sequenceInputGraph) {
        for (Edge edge : sequenceInputGraph.getEdges()) {
            this.edges.remove(edge);
        }
    }
    public void setEdges(List<Edge> edges) {
        this.edges = (ArrayList<Edge>) edges;
    }
    public void setConnections(boolean[][] cons) {
        this.connections = cons;
    }
    public ArrayList<String> getVertices() {
        return vertices;
    }
    public void setVertices(ArrayList<String> vertices) {
        this.vertices = vertices;
    }
    public ArrayList<Edge> getEdges() {
        return edges;
    }
    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }
    @SuppressWarnings("unchecked")
    public Graph cloneGraph(int counter) {

        String addition = "$" + counter;
        ArrayList<String> vClone = new ArrayList<>();
        ArrayList<Edge> eClone = new ArrayList<>();
        for (String v : this.getVertices()) {
            vClone.add(v + addition);
        }
        for (Edge e : this.getEdges()) {
            eClone.add(new Edge(e.getFirstVertex() + addition, e.getSecondVertex() + addition));
        }
        return new Graph(eClone, vClone, null);
    }
}
