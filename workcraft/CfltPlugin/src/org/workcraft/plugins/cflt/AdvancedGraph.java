package org.workcraft.plugins.cflt;

import java.util.*;

/**
 * A temporary Class which contains more detail that the regular Graph class
 * TODO: Remove this class once the SAT Solver solution is implemented
 */
public class AdvancedGraph extends Graph {

    //All common neighbours of each of the vertices of the edge
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
            if (vertexNameToAllNeighbours.containsKey(edge.getFirstVertexName())) {
                vertexNameToAllNeighbours.get(edge.getFirstVertexName()).add(edge.getSecondVertexName());
                vertexNameToUncoveredNeighbours.get(edge.getFirstVertexName()).add(edge.getSecondVertexName());
            } else {
                vertexNameToAllNeighbours.put(edge.getFirstVertexName(), new HashSet<>());
                vertexNameToAllNeighbours.get(edge.getFirstVertexName()).add(edge.getSecondVertexName());
                vertexNameToUncoveredNeighbours.put(edge.getFirstVertexName(), new HashSet<>());
                vertexNameToUncoveredNeighbours.get(edge.getFirstVertexName()).add(edge.getSecondVertexName());
            }
            if (vertexNameToAllNeighbours.containsKey(edge.getSecondVertexName())) {
                vertexNameToAllNeighbours.get(edge.getSecondVertexName()).add(edge.getFirstVertexName());
                vertexNameToUncoveredNeighbours.get(edge.getSecondVertexName()).add(edge.getFirstVertexName());
            } else {
                vertexNameToAllNeighbours.put(edge.getSecondVertexName(), new HashSet<>());
                vertexNameToAllNeighbours.get(edge.getSecondVertexName()).add(edge.getFirstVertexName());
                vertexNameToUncoveredNeighbours.put(edge.getSecondVertexName(), new HashSet<>());
                vertexNameToUncoveredNeighbours.get(edge.getSecondVertexName()).add(edge.getFirstVertexName());
            }
            allEdgeNames.put(edge.getFirstVertexName() + edge.getSecondVertexName(), edge);

            Set<String> firstVertexNeighbours = vertexNameToAllNeighbours.getOrDefault(edge.getFirstVertexName(), new HashSet<>());
            Set<String> secondVertexNeighbours = vertexNameToAllNeighbours.getOrDefault(edge.getSecondVertexName(), new HashSet<>());

            HashSet<String> commonNeighbours = new HashSet<>(firstVertexNeighbours);
            commonNeighbours.retainAll(secondVertexNeighbours);
            edgeNameToAllCommonNeighbours.put(edge, commonNeighbours);
            edgeToCn.put(edge, getCommonNeighbourCount(commonNeighbours));
        }
    }

    // Getting the edge with the highest number of uncovered interconnecting edges in it's closed neighbourhood
    public Edge getNextEdge() {
        return edgeToCn.entrySet().stream()
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

        if ((commonNeighbours == null) || commonNeighbours.isEmpty()) {
            Clique clique = new Clique();
            clique.addVertexName(edge.getFirstVertexName());
            clique.addVertexName(edge.getSecondVertexName());
            cliques.add(clique);
        } else {
            for (Clique maxClique : allMaxCliques) {
                if (maxClique.getVertexNames().contains(edge.getFirstVertexName())
                        && maxClique.getVertexNames().contains(edge.getSecondVertexName())) {
                    HashSet<String> usedVertexNameSet = new HashSet<>(maxClique.getVertexNames());
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
        for (Clique clique : edgeCliqueCover) {
            for (String firstVertex : clique.getVertexNames()) {
                for (String secondVertex : clique.getVertexNames()) {
                    if (!firstVertex.equals(secondVertex)) {
                        if (allEdgeNames.containsKey(firstVertex + secondVertex)) {
                            coveredEdges.add(allEdgeNames.get(firstVertex + secondVertex));
                        }
                        if (allEdgeNames.containsKey(secondVertex + firstVertex)) {
                            coveredEdges.add(allEdgeNames.get(secondVertex + firstVertex));
                        }
                        if (vertexNameToUncoveredNeighbours.get(firstVertex) != null) {
                            vertexNameToUncoveredNeighbours.get(firstVertex).remove(secondVertex);
                        }
                        if (vertexNameToUncoveredNeighbours.get(secondVertex) != null) {
                            vertexNameToUncoveredNeighbours.get(secondVertex).remove(firstVertex);
                        }
                    }
                }
            }
        }
        updateUncoveredCommonNeighbours();
    }

    private void updateUncoveredCommonNeighbours() {
        for (HashMap.Entry<String, Edge> entry : allEdgeNames.entrySet()) {
            String firstVertex = entry.getValue().getFirstVertexName();
            String secondVertex = entry.getValue().getSecondVertexName();

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
                if (!firstNeighbour.equals(secondNeighbour) &&
                        (allEdgeNames.containsKey(firstNeighbour + secondNeighbour) ||
                                allEdgeNames.containsKey(secondNeighbour + firstNeighbour))) {
                    count++;
                }
            }
        }

        count = (count / 2) + (commonNeighbours.size() * 2);
        return count;
    }
}


