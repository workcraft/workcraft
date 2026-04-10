package org.workcraft.plugins.cflt.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.utils.CliqueUtils;

public class EdgeCliqueCoverHeuristic {

    List<Clique> finalCliques = new ArrayList<>();

    Map<Vertex, Integer> vertexToUncoveredDegree = new HashMap<>();
    Map<Vertex, Integer> vertexToLocalUncoveredDegree = new HashMap<>();
    Map<Vertex, Set<Vertex>> vertexToAllNeighbours = new HashMap<>();

    Map<Edge, Integer> edgeToNoOfCliquesItsContainedIn = new HashMap<>();
    Map<Edge, Boolean> edgeToIsCovered = new HashMap<>();

    Set<Vertex> uncoveredVertices = new HashSet<>();
    Set<Edge> optionalEdges = new HashSet<>();

    public List<Clique> getEdgeCliqueCover(Graph graph, Set<Edge> optionalEdges, HeuristicType heuristicType) {

        this.optionalEdges = optionalEdges;
        this.vertexToAllNeighbours = graph.getVertexToAllNeighbours();
        initialiseHeuristicDataStructures(graph);

        int cliqueNumber = -1;
        int maxCliqueSize = 0;
        int currentCliqueSize = 0;

        while (!uncoveredVertices.isEmpty()) {
            Vertex i = selectVertex(heuristicType);

            while (vertexToUncoveredDegree.get(i) > 0) {
                if (currentCliqueSize > maxCliqueSize) {
                    maxCliqueSize = currentCliqueSize;
                }

                currentCliqueSize = 1;
                cliqueNumber += 1;

                finalCliques.add(cliqueNumber, new Clique());
                finalCliques.get(cliqueNumber).addVertex(i);

                HashSet<Vertex> localNeighbourhoodOfi = new HashSet<>(vertexToAllNeighbours.get(i));

                for (Vertex j : localNeighbourhoodOfi) {
                    Edge edge = new Edge(i, j);
                    boolean isCovered = edgeToIsCovered.get(edge);

                    vertexToLocalUncoveredDegree.put(j, 1 - (isCovered ? 1 : 0));
                }
                Vertex u = argMax(vertexToLocalUncoveredDegree, localNeighbourhoodOfi);

                while (vertexToLocalUncoveredDegree.get(u) > 0) {
                    boolean isOptional = true;

                    for (Vertex j : finalCliques.get(cliqueNumber).getVertices()) {
                        Edge edgeUj = new Edge(u, j);
                        Edge edgeJi = new Edge(j, i);

                        if (!edgeToIsCovered.get(edgeUj)) {
                            edgeToIsCovered.replace(edgeUj, true);

                            vertexToUncoveredDegree.merge(u, -1, Integer::sum);
                            vertexToUncoveredDegree.merge(j, -1, Integer::sum);
                        }

                        finalCliques.get(cliqueNumber).addEdge(edgeUj);
                        finalCliques.get(cliqueNumber).addEdge(edgeJi);

                        edgeToNoOfCliquesItsContainedIn.merge(edgeUj, 1, Integer::sum);
                        if (!optionalEdges.contains(edgeUj)) isOptional = false;
                    }
                    if (!isOptional) finalCliques.get(cliqueNumber).addVertex(u);

                    currentCliqueSize += 1;
                    localNeighbourhoodOfi.retainAll(vertexToAllNeighbours.get(u));

                    for (Vertex j : localNeighbourhoodOfi) {
                        Edge edge = new Edge(u, j);

                        if (!edgeToIsCovered.get(edge)) {
                            vertexToLocalUncoveredDegree.merge(j, 1, Integer::sum);
                        }
                    }

                    u = argMax(vertexToLocalUncoveredDegree, localNeighbourhoodOfi);
                    if (localNeighbourhoodOfi.isEmpty()) break;
                }
            }
            uncoveredVertices = updateUncoveredVertices();
        }

        CliqueUtils.expandNonMaximalCliques(
                maxCliqueSize,
                finalCliques,
                vertexToAllNeighbours,
                edgeToNoOfCliquesItsContainedIn);

        removeRedundantCliques();
        return filterCliquesConsistingOnlyOfOptionalEdges();
    }

    private void initialiseHeuristicDataStructures(Graph graph) {

        for (Edge edge : graph.getEdges()) {
            edgeToIsCovered.put(edge, false);
            edgeToNoOfCliquesItsContainedIn.put(edge, 0);
        }

        for (Vertex vertex : graph.getVertices()) {
            Set<Vertex> neighbours = vertexToAllNeighbours.get(vertex);
            if (neighbours != null && !neighbours.isEmpty()) {
                vertexToUncoveredDegree.put(vertex, neighbours.size());
                uncoveredVertices.add(vertex);
            } else {
                vertexToUncoveredDegree.put(vertex, 0);
            }
        }
    }

    private void removeRedundantCliques() {
        Iterator<Clique> iterator = finalCliques.iterator();
        while (iterator.hasNext()) {
            Clique clique = iterator.next();
            if (isCliqueRedundant(clique.getEdges())) {
                for (Edge edge : clique.getEdges()) {
                    edgeToNoOfCliquesItsContainedIn.merge(edge, -1, Integer::sum);
                }
                iterator.remove();
            }
        }
    }

    private List<Clique> filterCliquesConsistingOnlyOfOptionalEdges() {
        return finalCliques
                .stream()
                .map(finalClique ->
                    optionalEdges.containsAll(finalClique.getEdges())
                        ? new Clique()
                        : finalClique
                )
                .toList();
    }

    private Vertex argMin(Map<Vertex, Integer> uncoveredDegree, Set<Vertex> uncoveredVertices) {
        return uncoveredVertices
                .stream()
                .min(Comparator.comparingInt(uncoveredDegree::get))
                .orElse(null);
    }

    private Vertex argMax(Map<Vertex, Integer> uncoveredDegree, Set<Vertex> uncoveredVertices) {
        return uncoveredVertices
                .stream()
                .max(Comparator.comparingInt(uncoveredDegree::get))
                .orElse(null);
    }

    private boolean isCliqueRedundant(List<Edge> edges) {
        return edges
                .stream()
                .allMatch(edge -> edgeToNoOfCliquesItsContainedIn.getOrDefault(edge, 0) > 1);
    }

    private Set<Vertex> updateUncoveredVertices() {
        return uncoveredVertices.stream()
                .filter(v -> vertexToUncoveredDegree.get(v) > 0)
                .collect(Collectors.toSet());
    }

    private Vertex selectVertex(HeuristicType heuristicType) {
        return switch (heuristicType) {
            case MAXIMAL -> argMax(vertexToUncoveredDegree, uncoveredVertices);
            case MINIMAL -> argMin(vertexToUncoveredDegree, uncoveredVertices);
            case SEQUENCE -> uncoveredVertices.iterator().next();
        };
    }
}
