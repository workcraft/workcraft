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

public class SequenceHeuristic {

    public static List<List<String>> getEdgeCliqueCover(Graph graph, List<Edge> optionalEdges) {
        int cliqueNumber = -1;

        Map<String, Boolean> edgeNameToIsCovered = new HashMap<>();
        Map<String, Integer> vertexToUncoveredDegree = new HashMap<>();
        Map<String, HashSet<String>> allNeighboursToVertex = EdgeCliqueCoverUtils.initialiseNeighbours(graph);
        Map<String, Integer> edgeNameToNoOfCliquesItsContainedIn = new HashMap<>();
        Map<String, Integer> vertexToLocalUncoveredDegree = new HashMap<>();

        List<List<String>> finalCliquesAsVertices = new ArrayList<>();
        List<List<String>> finalCliquesAsEdgeNames = new ArrayList<>();

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
            vertexToUncoveredDegree.put(vertex,
                    allNeighboursToVertex.get(vertex) != null && !allNeighboursToVertex.get(vertex).isEmpty() ?
                            allNeighboursToVertex.get(vertex).size() : 0);
        }

        int maxCliqueSize = 0;
        int currentCliqueSize = 0;

        for (String i : graph.getVertices()) {
            while (vertexToUncoveredDegree.get(i) > 0) {
                if (currentCliqueSize > maxCliqueSize) {
                    maxCliqueSize = currentCliqueSize;
                }
                currentCliqueSize = 1;
                cliqueNumber += 1;

                finalCliquesAsVertices.add(cliqueNumber, new ArrayList<>());
                finalCliquesAsVertices.get(cliqueNumber).add(i);
                finalCliquesAsEdgeNames.add(new ArrayList<>());

                HashSet<String> localNeighbourhoodOfi = new HashSet<>(allNeighboursToVertex.get(i));
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
                    localNeighbourhoodOfi.retainAll(allNeighboursToVertex.get(u));

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
        }

        // If a clique only contains edges from the optional edge list it needs to be removed
        for (int x = 0; x < finalCliquesAsEdgeNames.size(); x++) {
            boolean containsOnlyOptionalEdges = true;
            for (String edge : finalCliquesAsEdgeNames.get(x)) {
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

        // Dealing with cliques which are not maximal
        int currentCliqueIndex = 0;
        for (List<String> finalClique : finalCliquesAsVertices) {
            // If the clique is not maximal
            if (finalClique.size() < maxCliqueSize && !finalClique.isEmpty()) {

                HashSet<String> neighboursOfFirstVertex = new HashSet<>(allNeighboursToVertex.get(finalClique.get(0)));
                ArrayList<String> verticesToBeAdded = new ArrayList<>(neighboursOfFirstVertex);
                for (int x = 1; x < finalClique.size(); x++) {
                    verticesToBeAdded.retainAll(allNeighboursToVertex.get(finalClique.get(x)));
                }

                while (!verticesToBeAdded.isEmpty()) {
                    String i = verticesToBeAdded.get(0);
                    finalClique.add(i);
                    verticesToBeAdded.retainAll(allNeighboursToVertex.get(i));

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
        // Removing redundant cliques
        for (int i = 0; i < finalCliquesAsVertices.size(); i++) {
            if (EdgeCliqueCoverUtils.isCliqueRedundant(edgeNameToNoOfCliquesItsContainedIn, finalCliquesAsEdgeNames.get(i))) {

                for (String edgeName : finalCliquesAsEdgeNames.get(i)) {
                    int temp = edgeNameToNoOfCliquesItsContainedIn.get(edgeName);
                    edgeNameToNoOfCliquesItsContainedIn.replace(edgeName, temp - 1);
                }

                finalCliquesAsEdgeNames.remove(i);
                finalCliquesAsVertices.remove(i);
            }
        }
        return finalCliquesAsVertices;
    }

}
