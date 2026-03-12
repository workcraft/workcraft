package org.workcraft.plugins.cflt.algorithms;

import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.graph.Graph;

import java.util.*;
import java.util.stream.Collectors;

public class EdgeCliqueCoverHeuristic {

    List<Clique> finalCliques = new ArrayList<>();

    Map<Vertex, Integer> vertexToUncoveredDegree = new HashMap<>();
    Map<Vertex, Integer> vertexToLocalUncoveredDegree = new HashMap<>();
    Map<Vertex, Set<Vertex>> vertexToAllNeighbours = new HashMap<>();

    Map<Edge, Integer> edgeToNoOfCliquesItsContainedIn = new HashMap<>();
    Map<Edge, Boolean> edgeToIsCovered = new HashMap<>();

    Set<Vertex> uncoveredVertices = new HashSet<>();
    Set<Edge> optionalEdges = new HashSet<>();

    public List<Clique> getEdgeCliqueCover(Graph graph, List<Edge> optionalEdges, HeuristicType heuristicType) {

        initialiseHeuristicDataStructures(graph);
        this.optionalEdges = new HashSet<>(optionalEdges);

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

        handleNonMaximalCliques(maxCliqueSize);
        removeRedundantCliques();
        return removeCliquesConsistingOfOptionalEdges();
    }

    private void initialiseHeuristicDataStructures(Graph graph) {
        for (Edge edge : graph.getEdges()) {
            vertexToAllNeighbours
                    .computeIfAbsent(edge.firstVertex(), k -> new HashSet<>())
                    .add(edge.secondVertex());
            vertexToAllNeighbours
                    .computeIfAbsent(edge.secondVertex(), k -> new HashSet<>())
                    .add(edge.firstVertex());
        }

        for (Edge edge : graph.getEdges()) {
            edgeToIsCovered.put(edge, false);
            edgeToNoOfCliquesItsContainedIn.put(edge, 0);
        }

        for (Vertex vertexName : graph.getVertices()) {
            Set<Vertex> neighbours = vertexToAllNeighbours.get(vertexName);
            if (neighbours != null && !neighbours.isEmpty()) {
                vertexToUncoveredDegree.put(vertexName, neighbours.size());
                uncoveredVertices.add(vertexName);
            } else {
                vertexToUncoveredDegree.put(vertexName, 0);
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

    private List<Clique> removeCliquesConsistingOfOptionalEdges() {
        return finalCliques
                .stream()
                .map(finalClique -> {
                    List<Edge> edges = finalClique.getEdges();
                    boolean containsOnlyOptionalEdges = optionalEdges.containsAll(edges);
                    return containsOnlyOptionalEdges
                            ? new Clique()
                            : finalClique;
                })
                .toList();
    }

    private void handleNonMaximalCliques(int maxCliqueSize) {
        int currentCliqueIndex = 0;

        for (Clique clique : finalCliques) {
            if (clique.getVertices().size() < maxCliqueSize && !clique.getVertices().isEmpty()) {
                Vertex firstClique = clique.getVertices().get(0);
                Set<Vertex> firstCliqueNeighbours = vertexToAllNeighbours.get(firstClique);

                Set<Vertex> neighboursOfFirstVertex = new HashSet<>(firstCliqueNeighbours);
                List<Vertex> verticesToBeAdded = new ArrayList<>(neighboursOfFirstVertex);

                for (int x = 1; x < clique.getVertices().size(); x++) {
                    Vertex currentClique = clique.getVertices().get(x);
                    Set<Vertex> currentCliqueNeighbours = vertexToAllNeighbours.get(currentClique);
                    verticesToBeAdded.retainAll(currentCliqueNeighbours);
                }

                while (!verticesToBeAdded.isEmpty()) {
                    Vertex i = verticesToBeAdded.get(0);
                    clique.addVertex(i);
                    Set<Vertex> neighboursOfi = vertexToAllNeighbours.get(i);
                    verticesToBeAdded.retainAll(neighboursOfi);

                    for (Vertex s : clique.getVertices()) {
                        if (!i.equals(s)) {
                            Edge edge = new Edge(i, s);
                            finalCliques.get(currentCliqueIndex).addEdge(edge);
                            edgeToNoOfCliquesItsContainedIn.merge(edge, 1, Integer::sum);
                        }
                    }
                }
            }
            currentCliqueIndex++;
        }
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
