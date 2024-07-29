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
        }

        for (Edge e : graph.getEdges()) {
            @SuppressWarnings("unchecked")
            HashSet<String> cn = (HashSet<String>) vertexNameToAllNeighbours.get(e.getFirstVertexName()).clone();
            cn.retainAll(vertexNameToAllNeighbours.get(e.getSecondVertexName()));
            edgeNameToAllCommonNeighbours.put(e, cn);

            int c = 0;
            for (String n1 : cn) {
                for (String n2 : cn) {
                    if ((allEdgeNames.containsKey(n1 + n2) || allEdgeNames.containsKey(n2 + n1)) && !n1.equals(n2)) { c++; }
                }
            }
            c /= 2;
            c += cn.size() * 2;
            edgeToCn.put(e, c);
        }
    }

    public void updateSets(List<Clique> ecc) {
        coveredEdges = new HashSet<>();
        for (Clique clique : ecc) {
            for (String v1 : clique.getVertexNames()) {
                for (String v2 : clique.getVertexNames()) {
                    if (allEdgeNames.containsKey(v1 + v2) && !v1.equals(v2)) {
                        coveredEdges.add(allEdgeNames.get(v1 + v2));
                    }
                    if (allEdgeNames.containsKey(v2 + v1) && !v1.equals(v2)) {
                        coveredEdges.add(allEdgeNames.get(v2 + v1));
                    }
                    if (!v1.equals(v2)) {
                        if (vertexNameToUncoveredNeighbours.get(v1) != null) {
                            vertexNameToUncoveredNeighbours.get(v1).remove(v2);
                        }
                        if (vertexNameToUncoveredNeighbours.get(v2) != null) {
                            vertexNameToUncoveredNeighbours.get(v2).remove(v1);
                        }
                    }
                }
            }
        }
        updateUncoveredCommonNeighbours();
    }

    public void updateUncoveredCommonNeighbours() {
        for (HashMap.Entry<String, Edge> set : allEdgeNames.entrySet()) {
            @SuppressWarnings("unchecked")
            HashSet<String> cn = (HashSet<String>) vertexNameToAllNeighbours.get(set.getValue().getFirstVertexName()).clone();
            cn.retainAll(vertexNameToUncoveredNeighbours.get(set.getValue().getSecondVertexName()));
            int c = 0;
            for (String n1 : cn) {
                for (String n2 : cn) {
                    if ((allEdgeNames.containsKey(n1 + n2) || allEdgeNames.containsKey(n2 + n1)) && !n1.equals(n2)) { c++; }
                }
            }
            c /= 2;
            c += cn.size() * 2;
            edgeToCn.replace(set.getValue(), c);
        }
    }

    // Selecting the edge with the highest number of uncovered interconnecting edges in its closes neighbourhood
    public Edge getNextEdge() {
        int highest = -1;
        Edge selectedEdge = null;
        for (HashMap.Entry<Edge, Integer> set : edgeToCn.entrySet()) {
            if (!coveredEdges.contains(set.getKey())) {
                if (set.getValue() > highest) {
                    highest = set.getValue();
                    selectedEdge = set.getKey();
                }
            }
        }
        return selectedEdge;
    }

    public boolean isCovered(List<Clique> ecc, List<Edge> optionalEdges) {
        coveredEdges = new HashSet<>();
        usedVertexNameSets = new HashSet<>();
        updateSets(ecc);
        if (optionalEdges != null) {
            coveredEdges.addAll(optionalEdges);
        }
        return allEdgeNames.size() <= coveredEdges.size();
    }

    public List<Clique> getMaximalCliques(Edge e) {
        List<Clique> cliques = new ArrayList<>();
        HashSet<String> cn = edgeNameToAllCommonNeighbours.get(e);

        if ((cn == null) || cn.isEmpty()) {
            Clique clique = new Clique();
            clique.addVertexName(e.getFirstVertexName());
            clique.addVertexName(e.getSecondVertexName());
            cliques.add(clique);
        } else {
            for (Clique maxClique : allMaxCliques) {
                if (maxClique.getVertexNames().contains(e.getFirstVertexName()) && maxClique.getVertexNames().contains(e.getSecondVertexName())) {
                    HashSet<String> cliqueSet = new HashSet<>(maxClique.getVertexNames());
                    if (!usedVertexNameSets.contains(cliqueSet)) {
                        cliques.add(maxClique);
                        usedVertexNameSets.add(cliqueSet);
                    }
                }
            }
        }
        return cliques;
    }
}


