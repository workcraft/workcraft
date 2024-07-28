package org.workcraft.plugins.cflt.algorithms;

import org.workcraft.plugins.cflt.Clique;
import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MaxMinHeuristic {

    public static List<Clique> getEdgeCliqueCover(Graph graph, List<Edge> optionalEdges, boolean isMaxHeuristic)  {
        int cliqueNumber = -1;

        Map<String, Boolean> edgeNameToIsCovered = new HashMap<>();
        Map<String, Integer> vertexNameToUncoveredDegree = new HashMap<>();
        Map<String, HashSet<String>> vertexNameToAllNeighbours = EdgeCliqueCoverUtils.getVertexNameToNeighbours(graph);
        Map<String, Integer> edgeNameToNoOfCliquesItsContainedIn = new HashMap<>();
        Map<String, Integer> vertexToLocalUncoveredDegree = new HashMap<>();

        List<Clique> finalCliques = new ArrayList<>();

        Set<String> uncoveredVertexNames = new HashSet<>();
        Set<String> optionalEdgeNameSet = new HashSet<>();

        for (Edge edge : optionalEdges) {
            optionalEdgeNameSet.add(edge.getFirstVertex() + edge.getSecondVertex());
            optionalEdgeNameSet.add(edge.getSecondVertex() + edge.getFirstVertex());
        }

        for (Edge edge : graph.getEdges()) {
            edgeNameToIsCovered.put(edge.getFirstVertex() + edge.getSecondVertex(), false);
            edgeNameToIsCovered.put(edge.getSecondVertex() + edge.getFirstVertex(), false);

            edgeNameToNoOfCliquesItsContainedIn.put(edge.getFirstVertex() + edge.getSecondVertex(), 0);
            edgeNameToNoOfCliquesItsContainedIn.put(edge.getSecondVertex() + edge.getFirstVertex(), 0);
        }

        for (String vertexName : graph.getVertices()) {
            if (vertexNameToAllNeighbours.get(vertexName) != null && !vertexNameToAllNeighbours.get(vertexName).isEmpty()) {
                vertexNameToUncoveredDegree.put(vertexName, vertexNameToAllNeighbours.get(vertexName).size());
                uncoveredVertexNames.add(vertexName);
            } else {
                vertexNameToUncoveredDegree.put(vertexName, 0);
            }
        }

        int maxCliqueSize = 0;
        int currentCliqueSize = 0;

        while (!uncoveredVertexNames.isEmpty()) {
            String i = isMaxHeuristic ?
                    EdgeCliqueCoverUtils.argMax(vertexNameToUncoveredDegree, uncoveredVertexNames) :
                    EdgeCliqueCoverUtils.argMin(vertexNameToUncoveredDegree, uncoveredVertexNames);

            while (vertexNameToUncoveredDegree.get(i) > 0) {
                if (currentCliqueSize > maxCliqueSize) {
                    maxCliqueSize = currentCliqueSize;
                }
                currentCliqueSize = 1;
                cliqueNumber += 1;

                finalCliques.add(cliqueNumber, new Clique());
                finalCliques.get(cliqueNumber).addVertexName(i);

                HashSet<String> localNeighbourhoodOfi = new HashSet<>(vertexNameToAllNeighbours.get(i));
                for (String j : localNeighbourhoodOfi) {
                    vertexToLocalUncoveredDegree.put(j, 1 - (edgeNameToIsCovered.get(i + j) ? 1 : 0));
                }

                String u = EdgeCliqueCoverUtils.argMax(vertexToLocalUncoveredDegree, localNeighbourhoodOfi);
                while (vertexToLocalUncoveredDegree.get(u) > 0) {
                    boolean isOptional = true;
                    for (String j : finalCliques.get(cliqueNumber).getVertexNames()) {
                        if (!edgeNameToIsCovered.get(u + j)) {
                            edgeNameToIsCovered.replace(u + j, true);
                            edgeNameToIsCovered.replace(j + u, true);

                            int temp = vertexNameToUncoveredDegree.get(u);
                            vertexNameToUncoveredDegree.replace(u, temp - 1);
                            temp = vertexNameToUncoveredDegree.get(j);
                            vertexNameToUncoveredDegree.replace(j, temp - 1);
                        }
                        // Adding the key edges of the clique
                        finalCliques.get(cliqueNumber).addEdgeName(u + j);
                        finalCliques.get(cliqueNumber).addEdgeName(u + j);

                        // Updating the number of cliques the edge is contained in
                        int oldVal = edgeNameToNoOfCliquesItsContainedIn.get(u + j);
                        edgeNameToNoOfCliquesItsContainedIn.replace(u + j, oldVal + 1);
                        edgeNameToNoOfCliquesItsContainedIn.replace(j + u, oldVal + 1);

                        if (!optionalEdgeNameSet.contains(u + j)) {
                            isOptional = false;
                        }
                    }
                    if (!isOptional) {
                        finalCliques.get(cliqueNumber).addVertexName(u);
                    }
                    currentCliqueSize += 1;
                    localNeighbourhoodOfi.retainAll(vertexNameToAllNeighbours.get(u));

                    for (String j : localNeighbourhoodOfi) {

                        if (!edgeNameToIsCovered.get(u + j)) {
                            int temp = vertexToLocalUncoveredDegree.get(j);
                            vertexToLocalUncoveredDegree.replace(j, temp + 1);
                        }
                    }
                    u = EdgeCliqueCoverUtils.argMax(vertexToLocalUncoveredDegree, localNeighbourhoodOfi);
                    if (localNeighbourhoodOfi.isEmpty()) { break; }
                }
            }
            // Updating the set of uncovered vertices
            HashSet<String> updatedUncoveredVertices = new HashSet<>();
            for (String v : uncoveredVertexNames) {
                if (vertexNameToUncoveredDegree.get(v) > 0) {
                    updatedUncoveredVertices.add(v);
                }
            }
            uncoveredVertexNames = updatedUncoveredVertices;
        }

        int currentCliqueIndex = 0;
        // Dealing with cliques which are not maximal
        for (Clique finalClique : finalCliques) {
            // If the clique is not maximal
            if (finalClique.getVertexNames().size() < maxCliqueSize && !finalClique.getVertexNames().isEmpty()) {
                HashSet<String> neighboursOfFirstVertex =
                        new HashSet<>(vertexNameToAllNeighbours.get(finalClique.getVertexNames().get(0)));
                List<String> verticesToBeAdded = new ArrayList<>(neighboursOfFirstVertex);
                for (int i = 1; i < finalClique.getVertexNames().size(); i++) {
                    verticesToBeAdded.retainAll(vertexNameToAllNeighbours.get(finalClique.getVertexNames().get(i)));
                }

                while (!verticesToBeAdded.isEmpty()) {
                    String i = verticesToBeAdded.get(0);
                    finalClique.addVertexName(i);
                    verticesToBeAdded.retainAll(vertexNameToAllNeighbours.get(i));

                    for (String s : finalClique.getVertexNames()) {
                        if (!i.equals(s)) {
                            finalCliques.get(currentCliqueIndex).addEdgeName(i + s);
                            finalCliques.get(currentCliqueIndex).addEdgeName(s + i);

                            // Updating the number of cliques the edge is contained in
                            int oldVal = edgeNameToNoOfCliquesItsContainedIn.get(i + s);
                            edgeNameToNoOfCliquesItsContainedIn.replace(i + s, oldVal + 1);
                            edgeNameToNoOfCliquesItsContainedIn.replace(s + i, oldVal + 1);
                        }
                    }
                }
            }
            currentCliqueIndex++;
        }

        // If a clique only contains edges from the optional edge list it needs to be removed
        List<Clique> updatedCliques = finalCliques.stream().map(finalClique -> {
                    boolean containsOnlyOptionalEdges = optionalEdgeNameSet.containsAll(finalClique.getEdgeNames());
                    return containsOnlyOptionalEdges ? new Clique() : finalClique;
                })
                .toList();

        finalCliques.clear();
        finalCliques.addAll(updatedCliques);

        // Removing redundant cliques
        for (int i = 0; i < finalCliques.size(); i++) {
            if (EdgeCliqueCoverUtils.isCliqueRedundant(edgeNameToNoOfCliquesItsContainedIn, finalCliques.get(i).getEdgeNames())) {

                for (String edge : finalCliques.get(i).getEdgeNames()) {
                    int temp = edgeNameToNoOfCliquesItsContainedIn.get(edge);
                    edgeNameToNoOfCliquesItsContainedIn.replace(edge, temp - 1);
                }

                finalCliques.remove(i);
            }
        }
        return finalCliques;
    }

}
