package org.workcraft.plugins.cflt.algorithms;

import org.workcraft.plugins.cflt.Clique;
import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils.*;

public class MaxMinHeuristic {

    public static List<Clique> getEdgeCliqueCover(Graph graph, List<Edge> optionalEdges, boolean isMaxHeuristic)  {
        List<Clique> finalCliques = new ArrayList<>();

        Map<String, Integer> vertexNameToUncoveredDegree = new HashMap<>();
        Map<String, Integer> vertexNameToLocalUncoveredDegree = new HashMap<>();
        Map<String, Set<String>> vertexNameToAllNeighbours = new HashMap<>();

        Map<String, Integer> edgeNameToNoOfCliquesItsContainedIn = new HashMap<>();
        Map<String, Boolean> edgeNameToIsCovered = new HashMap<>();

        Set<String> uncoveredVertexNames = new HashSet<>();
        Set<String> optionalEdgeNameSet = new HashSet<>();

        initialiseHeuristicDataStructures(
                graph,
                vertexNameToAllNeighbours,
                optionalEdges,
                optionalEdgeNameSet,
                edgeNameToIsCovered,
                edgeNameToNoOfCliquesItsContainedIn
        );

        for (String vertexName : graph.getVertexNames()) {
            if (vertexNameToAllNeighbours.get(vertexName) != null && !vertexNameToAllNeighbours.get(vertexName).isEmpty()) {
                vertexNameToUncoveredDegree.put(vertexName, vertexNameToAllNeighbours.get(vertexName).size());
                uncoveredVertexNames.add(vertexName);
            } else {
                vertexNameToUncoveredDegree.put(vertexName, 0);
            }
        }

        int cliqueNumber = -1;
        int maxCliqueSize = 0;
        int currentCliqueSize = 0;

        while (!uncoveredVertexNames.isEmpty()) {
            String i = isMaxHeuristic ?
                    argMax(vertexNameToUncoveredDegree, uncoveredVertexNames) :
                    argMin(vertexNameToUncoveredDegree, uncoveredVertexNames);

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
                    vertexNameToLocalUncoveredDegree.put(j, 1 - (edgeNameToIsCovered.get(i + j) ? 1 : 0));
                }

                String u = argMax(vertexNameToLocalUncoveredDegree, localNeighbourhoodOfi);
                while (vertexNameToLocalUncoveredDegree.get(u) > 0) {
                    boolean isOptional = true;
                    for (String j : finalCliques.get(cliqueNumber).getVertexNames()) {
                        if (!edgeNameToIsCovered.get(u + j)) {
                            /*
                             * TODO:
                             *  -Come up with, and implement, a way to avoid having to store edges/ edge names twice
                             *  -Do this for Min, Max, Sequence heuristics and edge clique cover utils class
                             *  -There is probably a way to avoid having so many Maps & Sets as well
                             */
                            edgeNameToIsCovered.replace(u + j, true);
                            edgeNameToIsCovered.replace(j + u, true);

                            int temp = vertexNameToUncoveredDegree.get(u);
                            vertexNameToUncoveredDegree.replace(u, temp - 1);
                            temp = vertexNameToUncoveredDegree.get(j);
                            vertexNameToUncoveredDegree.replace(j, temp - 1);
                        }
                        // Adding the key edges of the clique
                        finalCliques.get(cliqueNumber).addEdgeName(u + j);
                        finalCliques.get(cliqueNumber).addEdgeName(j + i);

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
                            int temp = vertexNameToLocalUncoveredDegree.get(j);
                            vertexNameToLocalUncoveredDegree.replace(j, temp + 1);
                        }
                    }
                    u = argMax(vertexNameToLocalUncoveredDegree, localNeighbourhoodOfi);
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

        handleNonMaximalCliques(finalCliques, maxCliqueSize, edgeNameToNoOfCliquesItsContainedIn, vertexNameToAllNeighbours);
        removeCliquesConsistingOfOptionalEdges(finalCliques, optionalEdgeNameSet);
        removeRedundantCliques(finalCliques, edgeNameToNoOfCliquesItsContainedIn);
        return finalCliques;
    }

}
