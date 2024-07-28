package org.workcraft.plugins.cflt;

import java.util.*;

/**
 * A temporary Class which contains more detail that the regular Graph class
 * TODO: Remove this class once the SAT Solver solution is implemented
 */
public class AdvancedGraph extends Graph {
    private final HashMap<Edge, HashSet<String>> allCommonNeighbours = new HashMap<>();
    private final HashMap<String, HashSet<String>> vertexNameToNeighbours = new HashMap<>();
    private final HashMap<String, HashSet<String>> neighboursViaUncoveredEdges = new HashMap<>();
    private final HashMap<String, Edge> edgeNameToEdge = new HashMap<>();
    private final HashSet<HashSet<String>> usedCliques = new HashSet<>();
    private final List<Clique> allMaxCliques;
    private HashSet<Edge> coveredEdges = new HashSet<>();

    /**
     *  Cn is the number of edges interconnecting the common (uncovered) neighbours
     *  of first and second vertices of the edge (excluding the edge itself)
     */
    private final HashMap<Edge, Integer> edgeToCn = new HashMap<>();

    public AdvancedGraph(Graph graph, List<Clique> allMaxCliques) {
        this.allMaxCliques = allMaxCliques;

        for (Edge edge : graph.getEdges()) {
            if (vertexNameToNeighbours.containsKey(edge.getFirstVertex())) {
                vertexNameToNeighbours.get(edge.getFirstVertex()).add(edge.getSecondVertex());
                neighboursViaUncoveredEdges.get(edge.getFirstVertex()).add(edge.getSecondVertex());
            } else {
                vertexNameToNeighbours.put(edge.getFirstVertex(), new HashSet<>());
                vertexNameToNeighbours.get(edge.getFirstVertex()).add(edge.getSecondVertex());
                neighboursViaUncoveredEdges.put(edge.getFirstVertex(), new HashSet<>());
                neighboursViaUncoveredEdges.get(edge.getFirstVertex()).add(edge.getSecondVertex());
            }
            if (vertexNameToNeighbours.containsKey(edge.getSecondVertex())) {
                vertexNameToNeighbours.get(edge.getSecondVertex()).add(edge.getFirstVertex());
                neighboursViaUncoveredEdges.get(edge.getSecondVertex()).add(edge.getFirstVertex());
            } else {
                vertexNameToNeighbours.put(edge.getSecondVertex(), new HashSet<>());
                vertexNameToNeighbours.get(edge.getSecondVertex()).add(edge.getFirstVertex());
                neighboursViaUncoveredEdges.put(edge.getSecondVertex(), new HashSet<>());
                neighboursViaUncoveredEdges.get(edge.getSecondVertex()).add(edge.getFirstVertex());
            }
            edgeNameToEdge.put(edge.getFirstVertex() + edge.getSecondVertex(), edge);
        }

        for (Edge edge : graph.getEdges()) {
            HashSet<String> original = vertexNameToNeighbours.get(edge.getFirstVertex());
            HashSet<String> commonNeighbours = new HashSet<>(original);
            commonNeighbours.retainAll(vertexNameToNeighbours.get(edge.getSecondVertex()));
            allCommonNeighbours.put(edge, commonNeighbours);
            edgeToCn.put(edge, getCn(commonNeighbours));
        }
    }

    //is used
    public void updateSets(List<Clique> edgeCliqueCover) {
        coveredEdges = new HashSet<>();
        for (Clique clique : edgeCliqueCover) {
            for (String firstVertex : clique.getVertexNames()) {
                for (String secondVertex : clique.getVertexNames()) {
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

    // is used
    public void updateUncoveredCommonNeighbours() {
        for (HashMap.Entry<String, Edge> set : edgeNameToEdge.entrySet()) {
            HashSet<String> original = vertexNameToNeighbours.get(set.getValue().getFirstVertex());
            HashSet<String> commonNeighbours = new HashSet<>(original);
            commonNeighbours.retainAll(neighboursViaUncoveredEdges.get(set.getValue().getSecondVertex()));
            edgeToCn.replace(set.getValue(), getCn(commonNeighbours));
        }
    }

    public Edge getNextEdge() {
        return edgeToCn.entrySet().stream()
                .filter(entry -> !coveredEdges.contains(entry.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public boolean isCovered(List<Clique> edgeCliqueCover, List<Edge> optionalEdges) {
        coveredEdges = optionalEdges != null ? new HashSet<>(optionalEdges) : new HashSet<>();
        updateSets(edgeCliqueCover);
        return edgeNameToEdge.size() <= coveredEdges.size();
    }

    public List<Clique> getMaximalCliques(Edge edge) {
        List<Clique> cliques = new ArrayList<>();
        HashSet<String> cn = allCommonNeighbours.get(edge);

        if ((cn == null) || cn.isEmpty()) {
            Clique clique = new Clique();
            clique.addVertexName(edge.getFirstVertex());
            clique.addVertexName(edge.getSecondVertex());
            cliques.add(clique);
        } else {
            for (Clique maxClique : allMaxCliques) {
                if (maxClique.getVertexNames().contains(edge.getFirstVertex())
                        && maxClique.getVertexNames().contains(edge.getSecondVertex())) {
                    HashSet<String> cliqueSet = new HashSet<>(maxClique.getVertexNames());
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


