package org.workcraft.plugins.cflt.ecc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils;

public class MaxMinHeuristic {

    public static ArrayList<ArrayList<String>> getEdgeCliqueCover(Graph graph, ArrayList<Edge> optionalEdges, boolean isMaxHeuristic)  {
        int cliqueNumber = -1;

        HashMap<String, Boolean> edgeNameToIsCovered = new HashMap<>();
        HashMap<String, Integer> vertexToUncoveredDegree = new HashMap<>();
        HashMap<String, HashSet<String>> vertexToAllNeighbours = EdgeCliqueCoverUtils.initialiseNeighbours(graph);
        HashMap<String, Integer> edgeNameToNoOfCliquesItsContainedIn = new HashMap<>();
        HashMap<String, Integer> vertexToLocalUncoveredDegree = new HashMap<>();

        ArrayList<ArrayList<String>> finalCliquesAsVertices = new ArrayList<>();
        ArrayList<ArrayList<String>> finalCliquesAsEdges = new ArrayList<>();

        HashSet<String> uncoveredVertices = new HashSet<>();
        HashSet<String> optionalEdgeSet = new HashSet<>();

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
            String i;
            if (isMaxHeuristic) {
                i = EdgeCliqueCoverUtils.argMax(vertexToUncoveredDegree, uncoveredVertices);
            } else {
                i = EdgeCliqueCoverUtils.argMin(vertexToUncoveredDegree, uncoveredVertices);
            }

            while (vertexToUncoveredDegree.get(i) > 0) {
                if (currentCliqueSize > maxCliqueSize) {
                    maxCliqueSize = currentCliqueSize;
                }
                currentCliqueSize = 1;
                cliqueNumber += 1;

                finalCliquesAsVertices.add(cliqueNumber, new ArrayList<>());
                finalCliquesAsVertices.get(cliqueNumber).add(i);
                finalCliquesAsEdges.add(new ArrayList<>());

                @SuppressWarnings("unchecked")
                HashSet<String> localNeighbourhoodOfi = (HashSet<String>) vertexToAllNeighbours.get(i).clone();
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
                        finalCliquesAsEdges.get(cliqueNumber).add(u + j);
                        finalCliquesAsEdges.get(cliqueNumber).add(j + u);

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
        for (ArrayList<String> finalClique : finalCliquesAsVertices) {
            // If the clique is not maximal
            if (finalClique.size() < maxCliqueSize && !finalClique.isEmpty()) {

                @SuppressWarnings("unchecked")
                HashSet<String> neighboursOfFirstVertex = (HashSet<String>) vertexToAllNeighbours.get(finalClique.get(0)).clone();
                ArrayList<String> verticesToBeAdded = new ArrayList<>(neighboursOfFirstVertex);

                for (int x = 1; x < finalClique.size(); x++) {
                    verticesToBeAdded.retainAll(vertexToAllNeighbours.get(finalClique.get(x)));
                }

                while (!verticesToBeAdded.isEmpty()) {
                    String i = verticesToBeAdded.get(0);
                    finalClique.add(i);
                    verticesToBeAdded.retainAll(vertexToAllNeighbours.get(i));

                    for (String s : finalClique) {
                        if (!i.equals(s)) {
                            finalCliquesAsEdges.get(currentCliqueIndex).add(i + s);
                            finalCliquesAsEdges.get(currentCliqueIndex).add(s + i);

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
        for (int x = 0; x < finalCliquesAsEdges.size(); x++) {
            boolean containsOnlyOptionalEdges = true;
            for (String edge : finalCliquesAsEdges.get(x)) {
                if (!optionalEdgeSet.contains(edge)) {
                    containsOnlyOptionalEdges = false;
                    break;
                }
            }
            if (containsOnlyOptionalEdges) {
                finalCliquesAsVertices.remove(x);
                finalCliquesAsVertices.add(x, new ArrayList<>());
            }
        }
        // Removing redundant cliques
        for (int x = 0; x < finalCliquesAsVertices.size(); x++) {
            if (EdgeCliqueCoverUtils.checkRedundancy(edgeNameToNoOfCliquesItsContainedIn, finalCliquesAsEdges.get(x))) {

                for (String edge : finalCliquesAsEdges.get(x)) {
                    int temp = edgeNameToNoOfCliquesItsContainedIn.get(edge);
                    edgeNameToNoOfCliquesItsContainedIn.replace(edge, temp - 1);
                }

                finalCliquesAsEdges.remove(x);
                finalCliquesAsVertices.remove(x);
            }
        }
        return finalCliquesAsVertices;
    }

}
