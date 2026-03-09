package org.workcraft.plugins.cflt.graph;

import java.util.*;

/**
 * A temporary class which contains more specific attributes than the regular Graph class
 * TODO: Remove this class once the SAT Solver (or any other exact, efficient) solution is implemented
 */
public class AdvancedGraph extends Graph {

    // All common neighbours of each of the vertices of the edge
    private final HashMap<Edge, HashSet<Vertex>> edgeToAllCommonNeighbours = new HashMap<>();

    // Number of edges interconnecting the common (uncovered) neighbours of both vertices of the edge (excluding the edge itself)
    private final HashMap<Edge, Integer> edgeToCn = new HashMap<>();
    private final HashSet<Edge> allEdges = new HashSet<>();
    private HashSet<Edge> coveredEdges = new HashSet<>();

    private final HashMap<Vertex, HashSet<Vertex>> vertexToAllNeighbours = new HashMap<>();
    private final HashMap<Vertex, HashSet<Vertex>> vertexToUncoveredNeighbours = new HashMap<>();

    // This set is used to make sure that the same cliques is not covered twice for example abc and acb
    private HashSet<HashSet<Vertex>> usedVertexSets = new HashSet<>();

    private final List<Clique> allMaxCliques;

    public AdvancedGraph(Graph graph, List<Clique> allMaxCliques) {
        this.allMaxCliques = allMaxCliques;

        for (Edge edge : graph.getEdges()) {
            Vertex firstVertex = edge.firstVertex();
            Vertex secondVertex = edge.secondVertex();

            if (!vertexToAllNeighbours.containsKey(firstVertex)) {
                vertexToAllNeighbours.put(firstVertex, new HashSet<>());
                vertexToUncoveredNeighbours.put(firstVertex, new HashSet<>());
            }
            vertexToAllNeighbours.get(firstVertex).add(secondVertex);
            vertexToUncoveredNeighbours.get(firstVertex).add(secondVertex);

            if (!vertexToAllNeighbours.containsKey(secondVertex)) {
                vertexToAllNeighbours.put(secondVertex, new HashSet<>());
                vertexToUncoveredNeighbours.put(secondVertex, new HashSet<>());
            }
            vertexToAllNeighbours.get(secondVertex).add(firstVertex);
            vertexToUncoveredNeighbours.get(secondVertex).add(firstVertex);

            allEdges.add(edge);
        }

        for (Edge edge : graph.getEdges()) {
            Vertex firstVertex = edge.firstVertex();
            Vertex secondVertex = edge.secondVertex();

            Set<Vertex> firstVertexNeighbours = vertexToAllNeighbours.getOrDefault(firstVertex, new HashSet<>());
            Set<Vertex> secondVertexNeighbours = vertexToAllNeighbours.getOrDefault(secondVertex, new HashSet<>());

            HashSet<Vertex> commonNeighbours = new HashSet<>(firstVertexNeighbours);
            commonNeighbours.retainAll(secondVertexNeighbours);
            edgeToAllCommonNeighbours.put(edge, commonNeighbours);
            edgeToCn.put(edge, getCommonNeighbourCount(commonNeighbours));
        }
    }

    // Getting the edge with the highest number of uncovered interconnecting edges in it's closed neighbourhood
    public Edge getNextEdge() {
        return edgeToCn.entrySet()
                .stream()
                .filter(entry -> !coveredEdges.contains(entry.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public boolean isCovered(List<Clique> edgeCliqueCover, List<Edge> optionalEdges) {
        coveredEdges = new HashSet<>();
        usedVertexSets = new HashSet<>();
        updateSets(edgeCliqueCover);
        if (optionalEdges != null) {
            coveredEdges.addAll(optionalEdges);
        }
        return allEdges.size() <= coveredEdges.size();
    }

    public List<Clique> getMaximalCliques(Edge edge) {
        List<Clique> cliques = new ArrayList<>();
        HashSet<Vertex> commonNeighbours = edgeToAllCommonNeighbours.get(edge);

        Vertex firstVertex = edge.firstVertex();
        Vertex secondVertex = edge.secondVertex();

        if ((commonNeighbours == null) || commonNeighbours.isEmpty()) {
            Clique clique = new Clique();
            clique.addVertex(firstVertex);
            clique.addVertex(secondVertex);
            cliques.add(clique);
        } else {
            for (Clique maxClique : allMaxCliques) {
                List<Vertex> vertices = maxClique.getVertices();
                boolean hasFirstVertex = vertices.contains(edge.firstVertex());
                boolean hasSecondVertex = vertices.contains(edge.secondVertex());

                if (hasFirstVertex && hasSecondVertex) {
                    HashSet<Vertex> usedVertexNameSet = new HashSet<>(vertices);

                    if (!usedVertexSets.contains(usedVertexNameSet)) {
                        cliques.add(maxClique);
                        usedVertexSets.add(usedVertexNameSet);
                    }
                }
            }
        }
        return cliques;
    }

    private void updateSets(List<Clique> edgeCliqueCover) {
        coveredEdges = new HashSet<>();
        edgeCliqueCover.forEach(this::updateCoveredEdges);
        updateUncoveredCommonNeighbours();
    }

    private void updateCoveredEdges(Clique clique) {
        for (Vertex firstVertex : clique.getVertices()) {
            for (Vertex secondVertex : clique.getVertices()) {
                if (firstVertex.equals(secondVertex)) continue;

                Edge edge = new Edge(firstVertex, secondVertex);
                if (allEdges.contains(edge)) {
                    coveredEdges.add(edge);
                }

                HashSet<Vertex> v1UncoveredNeighbours = vertexToUncoveredNeighbours.get(firstVertex);
                if (v1UncoveredNeighbours != null) v1UncoveredNeighbours.remove(secondVertex);

                HashSet<Vertex> v2UncoveredNeighbours = vertexToUncoveredNeighbours.get(secondVertex);
                if (v2UncoveredNeighbours != null) v2UncoveredNeighbours.remove(firstVertex);
            }
        }
    }

    private void updateUncoveredCommonNeighbours() {
        for (Edge edge : allEdges) {

            Vertex firstVertex = edge.firstVertex();
            Vertex secondVertex = edge.secondVertex();

            Set<Vertex> commonNeighbours = new HashSet<>(vertexToAllNeighbours.getOrDefault(firstVertex, new HashSet<>()));
            commonNeighbours.retainAll(vertexToUncoveredNeighbours.getOrDefault(secondVertex, new HashSet<>()));

            int count = getCommonNeighbourCount(commonNeighbours);
            edgeToCn.replace(edge, count);
        }
    }

    private int getCommonNeighbourCount(Set<Vertex> commonNeighbours) {
        int count = 0;
        for (Vertex firstNeighbour : commonNeighbours) {
            for (Vertex secondNeighbour : commonNeighbours) {
                boolean areVerticesEqual = firstNeighbour.equals(secondNeighbour);
                boolean isEdgePresent = allEdges.contains(new Edge(firstNeighbour, secondNeighbour));

                if (!areVerticesEqual && isEdgePresent) count++;
            }
        }
        count = (count / 2) + (commonNeighbours.size() * 2);
        return count;
    }
}


