package org.workcraft.plugins.cflt.utils;

import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.algorithms.ExhaustiveSearch;
import org.workcraft.plugins.cflt.algorithms.MaxMinHeuristic;
import org.workcraft.plugins.cflt.algorithms.SequenceHeuristic;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;

import java.util.*;

public final class EdgeCliqueCoverUtils {

    private EdgeCliqueCoverUtils() {
    }

    public static List<Clique> getEdgeCliqueCover(boolean isSequence, Mode mode, Graph inputGraph, Graph outputGraph) {
        Graph initialGraph = isSequence ? GraphUtils.join(inputGraph, outputGraph) : inputGraph;
        List<Edge> optionalEdges = isSequence ? inputGraph.getEdges() : new ArrayList<>();
        return switch (mode) {
        case SLOW_EXACT -> ExhaustiveSearch.getEdgeCliqueCover(initialGraph, optionalEdges);
        case FAST_SEQ -> SequenceHeuristic.getEdgeCliqueCover(initialGraph, optionalEdges);
        case FAST_MAX -> MaxMinHeuristic.getEdgeCliqueCover(initialGraph, optionalEdges, true);
        case FAST_MIN -> MaxMinHeuristic.getEdgeCliqueCover(initialGraph, optionalEdges, false);
        default -> new ArrayList<>();
        };
    }

    public static void initialiseHeuristicDataStructures(
            Graph graph,
            Map<String, Set<String>> vertexNameToAllNeighbours,
            List<Edge> optionalEdges,
            Set<String> optionalEdgeNameSet,
            Map<String, Boolean> edgeNameToIsCovered,
            Map<String, Integer> edgeNameToNoOfCliquesItsContainedIn) {
        for (Edge edge : graph.getEdges()) {
            vertexNameToAllNeighbours.putIfAbsent(edge.getFirstVertexName(), new HashSet<>());
            vertexNameToAllNeighbours.putIfAbsent(edge.getSecondVertexName(), new HashSet<>());
            vertexNameToAllNeighbours.get(edge.getFirstVertexName()).add(edge.getSecondVertexName());
            vertexNameToAllNeighbours.get(edge.getSecondVertexName()).add(edge.getFirstVertexName());
        }
        for (Edge edge : optionalEdges) {
            optionalEdgeNameSet.add(edge.getFirstVertexName() + edge.getSecondVertexName());
            optionalEdgeNameSet.add(edge.getSecondVertexName() + edge.getFirstVertexName());
        }
        for (Edge edge : graph.getEdges()) {
            edgeNameToIsCovered.put(edge.getFirstVertexName() + edge.getSecondVertexName(), false);
            edgeNameToIsCovered.put(edge.getSecondVertexName() + edge.getFirstVertexName(), false);

            edgeNameToNoOfCliquesItsContainedIn.put(edge.getFirstVertexName() + edge.getSecondVertexName(), 0);
            edgeNameToNoOfCliquesItsContainedIn.put(edge.getSecondVertexName() + edge.getFirstVertexName(), 0);
        }
    }

    public static void removeRedundantCliques(
            List<Clique> cliques,
            Map<String, Integer> edgeNameToNoOfCliquesItsContainedIn) {
        Iterator<Clique> iterator = cliques.iterator();
        while (iterator.hasNext()) {
            Clique clique = iterator.next();
            if (isCliqueRedundant(edgeNameToNoOfCliquesItsContainedIn, clique.getEdgeNames())) {
                for (String edge : clique.getEdgeNames()) {
                    int temp = edgeNameToNoOfCliquesItsContainedIn.get(edge);
                    edgeNameToNoOfCliquesItsContainedIn.replace(edge, temp - 1);
                }
                iterator.remove();
            }
        }
    }


    public static void removeCliquesConsistingOfOptionalEdges(List<Clique> cliques, Set<String> optionalEdgeNameSet) {
        List<Clique> updatedCliques = cliques.stream().map(finalClique -> {
            boolean containsOnlyOptionalEdges = optionalEdgeNameSet.containsAll(finalClique.getEdgeNames());
            return containsOnlyOptionalEdges ? new Clique() : finalClique;
        }).toList();

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

                Set<String> neighboursOfFirstVertex =
                        new HashSet<>(vertexNameToAllNeighbours.get(clique.getVertexNames().get(0)));
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

    private static boolean isCliqueRedundant(
            Map<String, Integer> edgeToNoOfCliquesItsContainedIn,
            List<String> cliqueAsEdgeNames) {
        return cliqueAsEdgeNames.stream()
                .allMatch(edge -> edgeToNoOfCliquesItsContainedIn.getOrDefault(edge, 0) > 1);
    }
}
