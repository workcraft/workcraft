package org.workcraft.plugins.cflt.algorithms;

import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils;

import java.util.*;

import static org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils.*;
import static org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils.removeRedundantCliques;

public class SequenceHeuristic {

    public static List<Clique> getEdgeCliqueCover(Graph graph, List<Edge> optionalEdges) {
        List<Clique> finalCliques = new ArrayList<>();

        HashMap<String, Integer> vertexNameToUncoveredDegree = new HashMap<>();
        HashMap<String, Integer> vertexNameToLocalUncoveredDegree = new HashMap<>();
        Map<String, Set<String>> vertexNameToAllNeighbours = new HashMap<>();

        HashMap<String, Integer> edgeNameToNoOfCliquesItsContainedIn = new HashMap<>();
        HashMap<String, Boolean> edgeNameToIsCovered = new HashMap<>();

        HashSet<String> optionalEdgeNameSet = new HashSet<>();

        initialiseHeuristicDataStructures(
                graph,
                vertexNameToAllNeighbours,
                optionalEdges,
                optionalEdgeNameSet,
                edgeNameToIsCovered,
                edgeNameToNoOfCliquesItsContainedIn
        );

        for (String vertex : graph.getVertexNames()) {
            if (vertexNameToAllNeighbours.get(vertex) != null && !vertexNameToAllNeighbours.get(vertex).isEmpty()) {
                vertexNameToUncoveredDegree.put(vertex, vertexNameToAllNeighbours.get(vertex).size());
            } else {
                vertexNameToUncoveredDegree.put(vertex, 0);
            }
        }
        int cliqueNumber = -1;
        int maxCliqueSize = 0;
        int currentCliqueSize = 0;

        for (String i : graph.getVertexNames()) {
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

                String u = EdgeCliqueCoverUtils.argMax(vertexNameToLocalUncoveredDegree, localNeighbourhoodOfi);
                while (vertexNameToLocalUncoveredDegree.get(u) > 0) {
                    boolean isOptional = true;
                    for (String j : finalCliques.get(cliqueNumber).getVertexNames()) {
                        if (!edgeNameToIsCovered.get(u + j) && !edgeNameToIsCovered.get(j + u)) {
                            edgeNameToIsCovered.replace(u + j, true);
                            edgeNameToIsCovered.replace(j + u, true);

                            int temp = vertexNameToUncoveredDegree.get(u);
                            vertexNameToUncoveredDegree.replace(u, temp - 1);
                            temp = vertexNameToUncoveredDegree.get(j);
                            vertexNameToUncoveredDegree.replace(j, temp - 1);
                        }

                        // Adding the key edges of the clique
                        finalCliques.get(cliqueNumber).addEdgeName(u + j);
                        finalCliques.get(cliqueNumber).addEdgeName(j + u);

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
                    u = EdgeCliqueCoverUtils.argMax(vertexNameToLocalUncoveredDegree, localNeighbourhoodOfi);
                    if (localNeighbourhoodOfi.isEmpty()) { break; }
                }
            }
        }

        handleNonMaximalCliques(finalCliques, maxCliqueSize, edgeNameToNoOfCliquesItsContainedIn, vertexNameToAllNeighbours);
        removeCliquesConsistingOfOptionalEdges(finalCliques, optionalEdgeNameSet);
        removeRedundantCliques(finalCliques, edgeNameToNoOfCliquesItsContainedIn);
        return finalCliques;
    }

}