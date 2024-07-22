package org.workcraft.plugins.cflt.algorithms;

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

    public static List<List<String>> getEdgeCliqueCover(Graph graph, List<Edge> optionalEdges, boolean isMaxHeuristic)  {
        int cliqueNumber = -1;

        Map<String, Boolean> edgeNameToIsCovered = new HashMap<>();
        Map<String, Integer> vertexToUncoveredDegree = new HashMap<>();
        Map<String, HashSet<String>> vertexToAllNeighbours = EdgeCliqueCoverUtils.initialiseNeighbours(graph);
        Map<String, Integer> edgeNameToNoOfCliquesItsContainedIn = new HashMap<>();
        Map<String, Integer> vertexToLocalUncoveredDegree = new HashMap<>();

        List<List<String>> finalCliquesAsVertices = new ArrayList<>();
        List<List<String>> finalCliquesAsEdgeNames = new ArrayList<>();

        Set<String> uncoveredVertices = new HashSet<>();
        Set<String> optionalEdgeSet = new HashSet<>();

        for (Edge edge : optionalEdges) {
            optionalEdgeSet.add(edge.getFirstVertex() + edge.getSecondVertex());
            optionalEdgeSet.add(edge.getSecondVertex() + edge.getFirstVertex());
        }

        for (Edge edge : graph.getEdges()) {
            edgeNameToIsCovered.put(edge.getFirstVertex() + edge.getSecondVertex(), false);
            edgeNameToIsCovered.put(edge.getSecondVertex() + edge.getFirstVertex(), false);

            edgeNameToNoOfCliquesItsContainedIn.put(edge.getFirstVertex() + edge.getSecondVertex(), 0);
            edgeNameToNoOfCliquesItsContainedIn.put(edge.getSecondVertex() + edge.getFirstVertex(), 0);
        }

        for (String vertex : graph.getVertices()) {
            if (vertexToAllNeighbours.get(vertex) != null && !vertexToAllNeighbours.get(vertex).isEmpty()) {
                vertexToUncoveredDegree.put(vertex, vertexToAllNeighbours.get(vertex).size());
                uncoveredVertices.add(vertex);
            } else {
                vertexToUncoveredDegree.put(vertex, 0);
            }
        }

        int maxCliqueSize = 0;
        int currentCliqueSize = 0;

        while (!uncoveredVertices.isEmpty()) {
            String i = isMaxHeuristic ?
                    EdgeCliqueCoverUtils.argMax(vertexToUncoveredDegree, uncoveredVertices) :
                    EdgeCliqueCoverUtils.argMin(vertexToUncoveredDegree, uncoveredVertices);

            while (vertexToUncoveredDegree.get(i) > 0) {
                if (currentCliqueSize > maxCliqueSize) {
                    maxCliqueSize = currentCliqueSize;
                }
                currentCliqueSize = 1;
                cliqueNumber += 1;

                finalCliquesAsVertices.add(cliqueNumber, new ArrayList<>());
                finalCliquesAsVertices.get(cliqueNumber).add(i);
                finalCliquesAsEdgeNames.add(new ArrayList<>());

                HashSet<String> localNeighbourhoodOfi = new HashSet<>(vertexToAllNeighbours.get(i));
                for (String j : localNeighbourhoodOfi) {
                    vertexToLocalUncoveredDegree.put(j, 1 - (edgeNameToIsCovered.get(i + j) ? 1 : 0));
                }

                String u = EdgeCliqueCoverUtils.argMax(vertexToLocalUncoveredDegree, localNeighbourhoodOfi);
                while (vertexToLocalUncoveredDegree.get(u) > 0) {
                    boolean isOptional = true;
                    for (String j : finalCliquesAsVertices.get(cliqueNumber)) {
                        if (!edgeNameToIsCovered.get(u + j) && !edgeNameToIsCovered.get(j + u)) {
                            edgeNameToIsCovered.replace(u + j, true);
                            edgeNameToIsCovered.replace(j + u, true);

                            int temp = vertexToUncoveredDegree.get(u);
                            vertexToUncoveredDegree.replace(u, temp - 1);
                            temp = vertexToUncoveredDegree.get(j);
                            vertexToUncoveredDegree.replace(j, temp - 1);
                        }
                        // Adding the key edges of the clique
                        finalCliquesAsEdgeNames.get(cliqueNumber).add(u + j);
                        finalCliquesAsEdgeNames.get(cliqueNumber).add(j + u);

                        // Updating the number of cliques the edge is contained in
                        int oldVal = edgeNameToNoOfCliquesItsContainedIn.get(u + j);
                        edgeNameToNoOfCliquesItsContainedIn.replace(u + j, oldVal + 1);
                        edgeNameToNoOfCliquesItsContainedIn.replace(j + u, oldVal + 1);

                        if (!optionalEdgeSet.contains(u + j) || !optionalEdgeSet.contains(j + u)) {
                            isOptional = false;
                        }
                    }
                    if (!isOptional) {
                        finalCliquesAsVertices.get(cliqueNumber).add(u);
                    }
                    currentCliqueSize += 1;
                    localNeighbourhoodOfi.retainAll(vertexToAllNeighbours.get(u));

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
            for (String v : uncoveredVertices) {
                if (vertexToUncoveredDegree.get(v) > 0) {
                    updatedUncoveredVertices.add(v);
                }
            }
            uncoveredVertices = updatedUncoveredVertices;
        }

        int currentCliqueIndex = 0;
        // Dealing with cliques which are not maximal
        for (List<String> finalClique : finalCliquesAsVertices) {
            // If the clique is not maximal
            if (finalClique.size() < maxCliqueSize && !finalClique.isEmpty()) {
                HashSet<String> neighboursOfFirstVertex = new HashSet<>(vertexToAllNeighbours.get(finalClique.get(0)));
                ArrayList<String> verticesToBeAdded = new ArrayList<>(neighboursOfFirstVertex);
                for (int i = 1; i < finalClique.size(); i++) {
                    verticesToBeAdded.retainAll(vertexToAllNeighbours.get(finalClique.get(i)));
                }

                while (!verticesToBeAdded.isEmpty()) {
                    String i = verticesToBeAdded.get(0);
                    finalClique.add(i);
                    verticesToBeAdded.retainAll(vertexToAllNeighbours.get(i));

                    for (String s : finalClique) {
                        if (!i.equals(s)) {
                            finalCliquesAsEdgeNames.get(currentCliqueIndex).add(i + s);
                            finalCliquesAsEdgeNames.get(currentCliqueIndex).add(s + i);

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
        for (int i = 0; i < finalCliquesAsEdgeNames.size(); i++) {
            boolean containsOnlyOptionalEdges = true;
            for (String edge : finalCliquesAsEdgeNames.get(i)) {
                if (!optionalEdgeSet.contains(edge)) {
                    containsOnlyOptionalEdges = false;
                    break;
                }
            }
            if (containsOnlyOptionalEdges) {
                finalCliquesAsVertices.remove(i);
                finalCliquesAsVertices.add(i, new ArrayList<>());
            }
        }
        // Removing redundant cliques
        for (int i = 0; i < finalCliquesAsVertices.size(); i++) {
            if (EdgeCliqueCoverUtils.isCliqueRedundant(edgeNameToNoOfCliquesItsContainedIn, finalCliquesAsEdgeNames.get(i))) {

                for (String edge : finalCliquesAsEdgeNames.get(i)) {
                    int temp = edgeNameToNoOfCliquesItsContainedIn.get(edge);
                    edgeNameToNoOfCliquesItsContainedIn.replace(edge, temp - 1);
                }

                finalCliquesAsEdgeNames.remove(i);
                finalCliquesAsVertices.remove(i);
            }
        }
        return finalCliquesAsVertices;
    }

}
