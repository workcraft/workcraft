package org.workcraft.plugins.cflt;

import java.util.*;

public class AdvancedGraph extends Graph {
    private final HashMap<Edge, HashSet<String>> allCommonNeighbours = new HashMap<>();
    private final HashMap<Edge, HashSet<String>> commonNeighboursViaUncoveredEdges = new HashMap<>();
    private HashMap<String, HashSet<String>> vertexToNeighbours = new HashMap<>();
    private HashMap<String, HashSet<String>> neighboursViaUncoveredEdges = new HashMap<>();

    /**
     *  Cn is the number of edges interconnecting the common (uncovered) neighbours
     *  of first and second vertices of the edge (excluding the edge itself)
     */
    private final HashMap<Edge, Integer> edgeToCn = new HashMap<>();
    private final HashMap<String, Edge> edgeNameToEdge = new HashMap<>();
    private HashSet<Edge> coveredEdges = new HashSet<>();
    private HashSet<HashSet<String>> usedCliques = new HashSet<>();

    private final List<List<String>> allMaxCliques;

    public AdvancedGraph(Graph graph, List<List<String>> allMaxCliques) {
        this.allMaxCliques = allMaxCliques;

        for (Edge edge : graph.getEdges()) {
            if (vertexToNeighbours.containsKey(edge.getFirstVertex())) {
                vertexToNeighbours.get(edge.getFirstVertex()).add(edge.getSecondVertex());
                neighboursViaUncoveredEdges.get(edge.getFirstVertex()).add(edge.getSecondVertex());
            } else {
                vertexToNeighbours.put(edge.getFirstVertex(), new HashSet<>());
                vertexToNeighbours.get(edge.getFirstVertex()).add(edge.getSecondVertex());
                neighboursViaUncoveredEdges.put(edge.getFirstVertex(), new HashSet<>());
                neighboursViaUncoveredEdges.get(edge.getFirstVertex()).add(edge.getSecondVertex());
            }
            if (vertexToNeighbours.containsKey(edge.getSecondVertex())) {
                vertexToNeighbours.get(edge.getSecondVertex()).add(edge.getFirstVertex());
                neighboursViaUncoveredEdges.get(edge.getSecondVertex()).add(edge.getFirstVertex());
            } else {
                vertexToNeighbours.put(edge.getSecondVertex(), new HashSet<>());
                vertexToNeighbours.get(edge.getSecondVertex()).add(edge.getFirstVertex());
                neighboursViaUncoveredEdges.put(edge.getSecondVertex(), new HashSet<>());
                neighboursViaUncoveredEdges.get(edge.getSecondVertex()).add(edge.getFirstVertex());
            }
            edgeNameToEdge.put(edge.getFirstVertex() + edge.getSecondVertex(), edge);
        }

        for (Edge edge : graph.getEdges()) {
            HashSet<String> original = vertexToNeighbours.get(edge.getFirstVertex());
            HashSet<String> commonNeighbours = new HashSet<>(original);
            commonNeighbours.retainAll(vertexToNeighbours.get(edge.getSecondVertex()));
            allCommonNeighbours.put(edge, commonNeighbours);
            commonNeighboursViaUncoveredEdges.put(edge, commonNeighbours);
            edgeToCn.put(edge, getCn(commonNeighbours));
        }
    }

    public void updateSets(List<List<String>> edgeCliqueCover) {
        coveredEdges = new HashSet<>();
        for (List<String> clique : edgeCliqueCover) {
            for (String firstVertex : clique) {
                for (String secondVertex : clique) {
                    if (edgeNameToEdge.containsKey(firstVertex + secondVertex) && !firstVertex.equals(secondVertex)) {
                        coveredEdges.add(edgeNameToEdge.get(firstVertex + secondVertex));
                    }
                    if (edgeNameToEdge.containsKey(secondVertex + firstVertex) && !firstVertex.equals(secondVertex)) {
                        coveredEdges.add(edgeNameToEdge.get(secondVertex + firstVertex));
                    }
                    if (!firstVertex.equals(secondVertex)) {
                        if (neighboursViaUncoveredEdges.get(firstVertex) != null) {
                            neighboursViaUncoveredEdges.get(firstVertex).remove(secondVertex);
                        }
                        if (neighboursViaUncoveredEdges.get(secondVertex) != null) {
                            neighboursViaUncoveredEdges.get(secondVertex).remove(firstVertex);
                        }
                    }
                }
            }
        }
        updateUncoveredCommonNeighbours();
    }

    public void updateUncoveredCommonNeighbours() {
        for (HashMap.Entry<String, Edge> set : edgeNameToEdge.entrySet()) {
            HashSet<String> original = vertexToNeighbours.get(set.getValue().getFirstVertex());
            HashSet<String> commonNeighbours = new HashSet<>(original);
            commonNeighbours.retainAll(neighboursViaUncoveredEdges.get(set.getValue().getSecondVertex()));
            commonNeighboursViaUncoveredEdges.replace(set.getValue(), commonNeighbours);
            edgeToCn.replace(set.getValue(), getCn(commonNeighbours));
        }
    }

    // Getting edge with the highest number of uncovered interconnecting edges in its closes neighbourhood
    public Edge getNextEdge() {
        return edgeToCn.entrySet().stream()
                .filter(entry -> !coveredEdges.contains(entry.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public boolean isCovered(List<List<String>> edgeCliqueCover, List<Edge> optionalEdges) {
        coveredEdges = optionalEdges != null ? new HashSet<>(optionalEdges) : new HashSet<>();
        updateSets(edgeCliqueCover);
        return edgeNameToEdge.size() <= coveredEdges.size();
    }

    public List<List<String>> getMaximalCliques(Edge edge) {
        List<List<String>> cliques = new ArrayList<>();
        HashSet<String> cn = allCommonNeighbours.get(edge);

        if ((cn == null) || cn.isEmpty()) {
            ArrayList<String> clique = new ArrayList<>();
            clique.add(edge.getFirstVertex());
            clique.add(edge.getSecondVertex());
            cliques.add(clique);
        } else {
            for (List<String> maxClique : allMaxCliques) {
                if (maxClique.contains(edge.getFirstVertex()) && maxClique.contains(edge.getSecondVertex())) {
                    HashSet<String> cliqueSet = new HashSet<>(maxClique);
                    if (!usedCliques.contains(cliqueSet)) {
                        cliques.add(maxClique);
                        usedCliques.add(cliqueSet);
                    }
                }
            }
        }
        return cliques;
    }

    private int getCn(HashSet<String> commonNeighbours) {
        int cn = 0;
        for (String firstNeighbour : commonNeighbours) {
            for (String secondNeighbour : commonNeighbours) {
                if ((edgeNameToEdge.containsKey(firstNeighbour + secondNeighbour)
                        || edgeNameToEdge.containsKey(secondNeighbour + firstNeighbour))
                        && !firstNeighbour.equals(secondNeighbour)) cn++;
            }
        }
        return cn / 2 + commonNeighbours.size() * 2;
    }
}


