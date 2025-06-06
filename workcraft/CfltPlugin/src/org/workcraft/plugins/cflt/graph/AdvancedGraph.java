package org.workcraft.plugins.cflt.graph;

import java.util.*;

/**
 * A temporary class which contains more specific attributes than the regular Graph class
 * TODO: Remove this class once the SAT Solver (or any other exact, efficient) solution is implemented
 */
public class AdvancedGraph extends Graph {

    // All common neighbours of each of the vertices of the edge
    private final HashMap<Edge, HashSet<String>> edgeNameToAllCommonNeighbours = new HashMap<>();

    // Number of edges interconnecting the common (uncovered) neighbours of both vertices of the edge (excluding the edge itself)
    private final HashMap<Edge, Integer> edgeToCn = new HashMap<>();
    private final HashMap<String, Edge> allEdgeNames = new HashMap<>();
    private HashSet<Edge> coveredEdges = new HashSet<>();

    private final HashMap<String, HashSet<String>> vertexNameToAllNeighbours = new HashMap<>();
    private final HashMap<String, HashSet<String>> vertexNameToUncoveredNeighbours = new HashMap<>();

    // This set is used to make sure that the same cliques is not covered twice for example abc and acb
    private HashSet<HashSet<String>> usedVertexNameSets = new HashSet<>();

    private final List<Clique> allMaxCliques;

    public AdvancedGraph(Graph graph, List<Clique> allMaxCliques) {
        this.allMaxCliques = allMaxCliques;

        for (Edge edge : graph.getEdges()) {
            String firstVertexName = edge.firstVertexName();
            String secondVertexName = edge.secondVertexName();

            if (!vertexNameToAllNeighbours.containsKey(firstVertexName)) {
                vertexNameToAllNeighbours.put(firstVertexName, new HashSet<>());
                vertexNameToUncoveredNeighbours.put(firstVertexName, new HashSet<>());
            }
            vertexNameToAllNeighbours.get(firstVertexName).add(secondVertexName);
            vertexNameToUncoveredNeighbours.get(firstVertexName).add(secondVertexName);

            if (!vertexNameToAllNeighbours.containsKey(secondVertexName)) {
                vertexNameToAllNeighbours.put(secondVertexName, new HashSet<>());
                vertexNameToUncoveredNeighbours.put(secondVertexName, new HashSet<>());
            }
            vertexNameToAllNeighbours.get(secondVertexName).add(firstVertexName);
            vertexNameToUncoveredNeighbours.get(secondVertexName).add(firstVertexName);

            allEdgeNames.put(firstVertexName + secondVertexName, edge);
        }

        for (Edge edge : graph.getEdges()) {
            String firstVertexName = edge.firstVertexName();
            String secondVertexName = edge.secondVertexName();

            Set<String> firstVertexNeighbours = vertexNameToAllNeighbours.getOrDefault(firstVertexName, new HashSet<>());
            Set<String> secondVertexNeighbours = vertexNameToAllNeighbours.getOrDefault(secondVertexName, new HashSet<>());

            HashSet<String> commonNeighbours = new HashSet<>(firstVertexNeighbours);
            commonNeighbours.retainAll(secondVertexNeighbours);
            edgeNameToAllCommonNeighbours.put(edge, commonNeighbours);
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
        usedVertexNameSets = new HashSet<>();
        updateSets(edgeCliqueCover);
        if (optionalEdges != null) {
            coveredEdges.addAll(optionalEdges);
        }
        return allEdgeNames.size() <= coveredEdges.size();
    }

    public List<Clique> getMaximalCliques(Edge edge) {
        List<Clique> cliques = new ArrayList<>();
        HashSet<String> commonNeighbours = edgeNameToAllCommonNeighbours.get(edge);

        String firstVertexName = edge.firstVertexName();
        String secondVertexName = edge.secondVertexName();

        if ((commonNeighbours == null) || commonNeighbours.isEmpty()) {
            Clique clique = new Clique();
            clique.addVertexName(firstVertexName);
            clique.addVertexName(secondVertexName);
            cliques.add(clique);
        } else {
            for (Clique maxClique : allMaxCliques) {
                List<String> vertexNames = maxClique.getVertexNames();
                boolean hasFirstVertex = vertexNames.contains(edge.firstVertexName());
                boolean hasSecondVertex = vertexNames.contains(edge.secondVertexName());

                if (hasFirstVertex && hasSecondVertex) {
                    HashSet<String> usedVertexNameSet = new HashSet<>(vertexNames);

                    if (!usedVertexNameSets.contains(usedVertexNameSet)) {
                        cliques.add(maxClique);
                        usedVertexNameSets.add(usedVertexNameSet);
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
        for (String firstVertex : clique.getVertexNames()) {
            for (String secondVertex : clique.getVertexNames()) {
                if (!firstVertex.equals(secondVertex)) {

                    String edgeName1 = firstVertex + secondVertex;
                    String edgeName2 = secondVertex + firstVertex;

                    if (allEdgeNames.containsKey(edgeName1)) {
                        coveredEdges.add(allEdgeNames.get(edgeName1));
                    }
                    if (allEdgeNames.containsKey(edgeName2)) {
                        coveredEdges.add(allEdgeNames.get(edgeName2));
                    }

                    HashSet<String> v1UncoveredNeighbours = vertexNameToUncoveredNeighbours.get(firstVertex);
                    if (v1UncoveredNeighbours != null) v1UncoveredNeighbours.remove(secondVertex);

                    HashSet<String> v2UncoveredNeighbours = vertexNameToUncoveredNeighbours.get(secondVertex);
                    if (v2UncoveredNeighbours != null) v2UncoveredNeighbours.remove(firstVertex);
                }
            }
        }
    }

    private void updateUncoveredCommonNeighbours() {
        for (HashMap.Entry<String, Edge> entry : allEdgeNames.entrySet()) {

            String firstVertex = entry.getValue().firstVertexName();
            String secondVertex = entry.getValue().secondVertexName();

            Set<String> commonNeighbours = new HashSet<>(vertexNameToAllNeighbours.getOrDefault(firstVertex, new HashSet<>()));
            commonNeighbours.retainAll(vertexNameToUncoveredNeighbours.getOrDefault(secondVertex, new HashSet<>()));

            int count = getCommonNeighbourCount(commonNeighbours);
            edgeToCn.replace(entry.getValue(), count);
        }
    }

    private int getCommonNeighbourCount(Set<String> commonNeighbours) {
        int count = 0;
        for (String firstNeighbour : commonNeighbours) {
            for (String secondNeighbour : commonNeighbours) {
                boolean areEqual = firstNeighbour.equals(secondNeighbour);
                boolean isV1V2Present = allEdgeNames.containsKey(firstNeighbour + secondNeighbour);
                boolean isV2V1Present = allEdgeNames.containsKey(secondNeighbour + firstNeighbour);

                if (!areEqual && (isV1V2Present || isV2V1Present)) count++;
            }
        }
        count = (count / 2) + (commonNeighbours.size() * 2);
        return count;
    }
}


