package org.workcraft.plugins.cflt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AdvancedGraph extends Graph {

    //all common neighbours of v1 and v2 of the edge
    private final HashMap<Edge, HashSet<String>> allCommonNeighbours = new HashMap<>();
    //only the common neighbours connected via uncovered edges of v1 and v2 of the edge
    private final HashMap<Edge, HashSet<String>> uncoveredCommonNeighbours = new HashMap<>();

    //all neighbours of the vertex
    HashMap<String, HashSet<String>> allNeighbours = new HashMap<>();
    //only the neighbours connected via uncovered edges
    HashMap<String, HashSet<String>> uncoveredNeighbours = new HashMap<>();

    //number of edges interconnecting the common (uncovered) neighbours of v1 and v2 of the edge (excluding the edge itself)
    private final HashMap<Edge, Integer> uncoveredCnInterconnectingEdgeNo = new HashMap<>();

    //edge in the form of s = v1 + v2
    private final HashMap<String, Edge> allEdges = new HashMap<>();

    private HashSet<Edge> coveredEdges = new HashSet<>();

    //this set is used to make sure that the same cliques is not covered twice for example abc and acb
    private HashSet<HashSet<String>> usedCliques = new HashSet<>();

    private final ArrayList<ArrayList<String>> allMaxCliques;

    public AdvancedGraph(Graph g, ArrayList<ArrayList<String>> allMaxCliques) {
        this.allMaxCliques = allMaxCliques;
        //initialising neighbours
        for (Edge e : g.getEdges()) {
            if (allNeighbours.containsKey(e.getFirstVertex())) {
                allNeighbours.get(e.getFirstVertex()).add(e.getSecondVertex());

                uncoveredNeighbours.get(e.getFirstVertex()).add(e.getSecondVertex());

            } else {
                allNeighbours.put(e.getFirstVertex(), new HashSet<>());
                allNeighbours.get(e.getFirstVertex()).add(e.getSecondVertex());

                uncoveredNeighbours.put(e.getFirstVertex(), new HashSet<>());
                uncoveredNeighbours.get(e.getFirstVertex()).add(e.getSecondVertex());
            }
            if (allNeighbours.containsKey(e.getSecondVertex())) {
                allNeighbours.get(e.getSecondVertex()).add(e.getFirstVertex());

                uncoveredNeighbours.get(e.getSecondVertex()).add(e.getFirstVertex());

            } else {
                allNeighbours.put(e.getSecondVertex(), new HashSet<>());
                allNeighbours.get(e.getSecondVertex()).add(e.getFirstVertex());

                uncoveredNeighbours.put(e.getSecondVertex(), new HashSet<>());
                uncoveredNeighbours.get(e.getSecondVertex()).add(e.getFirstVertex());
            }
            allEdges.put(e.getFirstVertex() + e.getSecondVertex(), e);
        }
        //initialising common neighbours
        for (Edge e : g.getEdges()) {
            @SuppressWarnings("unchecked")
            HashSet<String> cn = (HashSet<String>) allNeighbours.get(e.getFirstVertex()).clone();
            cn.retainAll(allNeighbours.get(e.getSecondVertex()));
            allCommonNeighbours.put(e, cn);
            uncoveredCommonNeighbours.put(e, cn);

            int c = 0;
            for (String n1 : cn) {
                for (String n2 : cn) {
                    if ((allEdges.containsKey(n1 + n2) || allEdges.containsKey(n2 + n1)) && !n1.equals(n2)) { c++; }
                }
            }
            c = c / 2;
            c += cn.size() * 2;
            // Number of edges interconnecting the common neighbours of v1 and v2 of the edge  (excluding the edge itself)
            HashMap<Edge, Integer> cnInterconnectingEdgeNo = new HashMap<>();
            cnInterconnectingEdgeNo.put(e, c);
            uncoveredCnInterconnectingEdgeNo.put(e, c);
        }
    }

    public  ArrayList<ArrayList<String>> reduceViaRule2() {
        ArrayList<ArrayList<String>> cliques = new ArrayList<>();
        for (HashMap.Entry<Edge, Integer> set : uncoveredCnInterconnectingEdgeNo.entrySet()) {
            if (!coveredEdges.contains(set.getKey())) {
                // Number of common neighbours of the two vertices of the edge
                int cn = uncoveredCommonNeighbours.get(set.getKey()).size();
                // Connections between the common neighbours excluding the edge itself
                int con = set.getValue();
                // The number of connections (con) required between v1 and v2 and all of their common neighbours (cn),
                // for them to induce a clique (excluding the connection between v1 and v2) is as follows: con = 1/2(cn^2+3cn)
                if (con == (Math.pow(cn, 2) + 3 * cn) / 2) {
                    ArrayList<String> clique = new ArrayList<>(uncoveredCommonNeighbours.get(set.getKey()));
                    HashSet<String> cliqueSet = new HashSet<>(uncoveredCommonNeighbours.get(set.getKey()));

                    clique.add(set.getKey().getFirstVertex());
                    clique.add(set.getKey().getSecondVertex());

                    cliqueSet.add(set.getKey().getFirstVertex());
                    cliqueSet.add(set.getKey().getSecondVertex());

                    if (!usedCliques.contains(cliqueSet)) {
                        cliques.add(clique);
                        usedCliques.add(cliqueSet);
                        updateSet(clique);
                    }
                }
            }
        }
        return cliques;
    }

    private void updateSet(ArrayList<String> clique) {
        for (String v1 : clique) {
            for (String v2 : clique) {
                if (allEdges.containsKey(v1 + v2) && !v1.equals(v2)) {
                    coveredEdges.add(allEdges.get(v1 + v2));
                }
                if (allEdges.containsKey(v2 + v1) && !v1.equals(v2)) {
                    coveredEdges.add(allEdges.get(v2 + v1));
                }
                if (!v1.equals(v2)) {
                    if (uncoveredNeighbours.get(v1) != null) {
                        uncoveredNeighbours.get(v1).remove(v2);
                    }
                    if (uncoveredNeighbours.get(v2) != null) {
                        uncoveredNeighbours.get(v2).remove(v1);
                    }
                }
            }
        }
        updateUncoveredCommonNeighbours();
    }

    public void updateSets(ArrayList<ArrayList<String>> ecc) {
        coveredEdges = new HashSet<>();
        for (ArrayList<String> clique : ecc) {
            for (String v1 : clique) {
                for (String v2 : clique) {
                    if (allEdges.containsKey(v1 + v2) && !v1.equals(v2)) {
                        coveredEdges.add(allEdges.get(v1 + v2));
                    }
                    if (allEdges.containsKey(v2 + v1) && !v1.equals(v2)) {
                        coveredEdges.add(allEdges.get(v2 + v1));
                    }
                    if (!v1.equals(v2)) {
                        if (uncoveredNeighbours.get(v1) != null) {
                            uncoveredNeighbours.get(v1).remove(v2);
                        }
                        if (uncoveredNeighbours.get(v2) != null) {
                            uncoveredNeighbours.get(v2).remove(v1);
                        }
                    }
                }
            }
        }
        updateUncoveredCommonNeighbours();
    }

    public void updateUncoveredCommonNeighbours() {
        for (HashMap.Entry<String, Edge> set : allEdges.entrySet()) {
            @SuppressWarnings("unchecked")
            HashSet<String> cn = (HashSet<String>) allNeighbours.get(set.getValue().getFirstVertex()).clone();
            cn.retainAll(uncoveredNeighbours.get(set.getValue().getSecondVertex()));
            uncoveredCommonNeighbours.replace(set.getValue(), cn);
            int c = 0;
            for (String n1 : cn) {
                for (String n2 : cn) {
                    if ((allEdges.containsKey(n1 + n2) || allEdges.containsKey(n2 + n1)) && !n1.equals(n2)) { c++; }
                }
            }
            c = c / 2;
            c += cn.size() * 2;
            uncoveredCnInterconnectingEdgeNo.replace(set.getValue(), c);
        }
    }

    // Selecting the edge with the highest number of uncovered interconnecting edges in its closes neighbourhood
    public Edge selectEdge() {
        int highest = -1;
        Edge selectedEdge = null;
        for (HashMap.Entry<Edge, Integer> set : uncoveredCnInterconnectingEdgeNo.entrySet()) {
            if (!coveredEdges.contains(set.getKey())) {
                if (set.getValue() > highest) {
                    highest = set.getValue();
                    selectedEdge = set.getKey();
                }
            }
        }
        return selectedEdge;
    }

    public boolean isCovered(ArrayList<ArrayList<String>> ecc, ArrayList<Edge> optionalEdges) {
        coveredEdges = new HashSet<>();
        usedCliques = new HashSet<>();
        updateSets(ecc);
        if (optionalEdges != null) {
            coveredEdges.addAll(optionalEdges);
        }
        return allEdges.size() <= coveredEdges.size();
    }

    public ArrayList<ArrayList<String>> getMaximalCliques(Edge e) {
        ArrayList<ArrayList<String>> cliques = new ArrayList<>();
        HashSet<String> cn = allCommonNeighbours.get(e);

        if ((cn == null) || cn.isEmpty()) {
            ArrayList<String> clique = new ArrayList<>();
            clique.add(e.getFirstVertex());
            clique.add(e.getSecondVertex());
            cliques.add(clique);
        } else {
            for (ArrayList<String> maxClique : allMaxCliques) {
                if (maxClique.contains(e.getFirstVertex()) && maxClique.contains(e.getSecondVertex())) {
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

}


