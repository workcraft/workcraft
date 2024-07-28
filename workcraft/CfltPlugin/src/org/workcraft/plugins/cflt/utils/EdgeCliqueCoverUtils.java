package org.workcraft.plugins.cflt.utils;

import org.workcraft.plugins.cflt.Clique;
import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.algorithms.ExhaustiveSearch;
import org.workcraft.plugins.cflt.algorithms.MaxMinHeuristic;
import org.workcraft.plugins.cflt.algorithms.SequenceHeuristic;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

public final class EdgeCliqueCoverUtils {

    private EdgeCliqueCoverUtils() {
    }

    public static List<List<String>> getEdgeCliqueCover(boolean isSequence, Mode mode, Graph inputG, Graph outputG) {
        Graph initGraph = isSequence ? GraphUtils.join(inputG, outputG) : inputG;
        List<Edge> optionalEdges = isSequence ? inputG.getEdges() : new ArrayList<>();
        switch (mode) {
        case SLOW_EXACT:
            return ExhaustiveSearch.getEdgeCliqueCover(initGraph, optionalEdges);
        case FAST_SEQ:
            return SequenceHeuristic.getEdgeCliqueCover(initGraph, optionalEdges);
        case FAST_MAX:
            return MaxMinHeuristic.getEdgeCliqueCover(initGraph, optionalEdges, true);
        case FAST_MIN:
            return MaxMinHeuristic.getEdgeCliqueCover(initGraph, optionalEdges, false);
        }
        return new ArrayList<>();
    }

    public static  void initialiseHeuristicDataStructures(
            Graph graph,
            Map<String, Set<String>> vertexNameToAllNeighbours,
            List<Edge> optionalEdges,
            Set<String> optionalEdgeNameSet,
            Map<String, Boolean> edgeNameToIsCovered,
            Map<String, Integer> edgeNameToNoOfCliquesItsContainedIn) {
        for (Edge edge : graph.getEdges()) {
            vertexNameToAllNeighbours.putIfAbsent(edge.getFirstVertex(), new HashSet<>());
            vertexNameToAllNeighbours.putIfAbsent(edge.getSecondVertex(), new HashSet<>());
            vertexNameToAllNeighbours.get(edge.getFirstVertex()).add(edge.getSecondVertex());
            vertexNameToAllNeighbours.get(edge.getSecondVertex()).add(edge.getFirstVertex());
        }
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
    }

    public static void removeRedundantCliques(List<Clique> cliques, Map<String, Integer> edgeNameToNoOfCliquesItsContainedIn) {
        for (int i = 0; i < cliques.size(); i++) {
            if (isCliqueRedundant(edgeNameToNoOfCliquesItsContainedIn, cliques.get(i).getEdgeNames())) {
                for (String edge : cliques.get(i).getEdgeNames()) {
                    int temp = edgeNameToNoOfCliquesItsContainedIn.get(edge);
                    edgeNameToNoOfCliquesItsContainedIn.replace(edge, temp - 1);
                }
                cliques.remove(i);
            }
        }
    }

    public static void removeCliquesConsistingOfOptionalEdges(List<Clique> cliques, Set<String> optionalEdgeNameSet) {
        List<Clique> updatedCliques = cliques.stream().map(finalClique -> {
                    boolean containsOnlyOptionalEdges = optionalEdgeNameSet.containsAll(finalClique.getEdgeNames());
                    return containsOnlyOptionalEdges ? new Clique() : finalClique;
                })
                .toList();

        cliques.clear();
        cliques.addAll(updatedCliques);
    }

    public static void handleNonMaximalCliques(
            List<Clique> cliques,
            int maxCliqueSize,
            Map<String, Integer> edgeNameToNoOfCliquesItsContainedIn,
            Map<String, Set<String>> vertexNameToAllNeighbours) {

        int currentCliqueIndex = 0;
        for (Clique clique : cliques) {
            // If the clique is not maximal
            if (clique.getVertexNames().size() < maxCliqueSize && !clique.getVertexNames().isEmpty()) {

                Set<String> neighboursOfFirstVertex = new HashSet<>(vertexNameToAllNeighbours.get(clique.getVertexNames().get(0)));
                List<String> verticesToBeAdded = new ArrayList<>(neighboursOfFirstVertex);
                for (int x = 1; x < clique.getVertexNames().size(); x++) {
                    verticesToBeAdded.retainAll(vertexNameToAllNeighbours.get(clique.getVertexNames().get(x)));
                }

                while (!verticesToBeAdded.isEmpty()) {
                    String i = verticesToBeAdded.get(0);
                    clique.addVertexName(i);
                    verticesToBeAdded.retainAll(vertexNameToAllNeighbours.get(i));

                    for (String s : clique.getVertexNames()) {
                        if (!i.equals(s)) {
                            cliques.get(currentCliqueIndex).addEdgeName(i + s);
                            cliques.get(currentCliqueIndex).addEdgeName(s + i);

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
    }

    public static String argMin(Map<String, Integer> uncoveredDegree, Set<String> uncoveredVertices) {
        return uncoveredVertices.stream()
                .min(Comparator.comparingInt(uncoveredDegree::get))
                .orElse(null);
    }

    public static String argMax(Map<String, Integer> uncoveredDegree, Set<String> uncoveredVertices) {
        return uncoveredVertices.stream()
                .max(Comparator.comparingInt(uncoveredDegree::get))
                .orElse(null);
    }

    private static boolean isCliqueRedundant(Map<String, Integer> edgeToNoOfCliquesItsContainedIn, List<String> cliqueAsEdgeNames) {
        return cliqueAsEdgeNames.stream()
                .allMatch(edge -> edgeToNoOfCliquesItsContainedIn.getOrDefault(edge, 0) > 1);
    }
}
