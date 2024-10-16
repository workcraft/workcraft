package org.workcraft.plugins.cflt.algorithms;

import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;

import java.util.*;

public class EdgeCliqueCoverHeuristic {

    List<Clique> finalCliques = new ArrayList<>();

    Map<String, Integer> vertexNameToUncoveredDegree = new HashMap<>();
    Map<String, Integer> vertexNameToLocalUncoveredDegree = new HashMap<>();
    Map<String, Set<String>> vertexNameToAllNeighbours = new HashMap<>();

    Map<String, Integer> edgeNameToNoOfCliquesItsContainedIn = new HashMap<>();
    Map<String, Boolean> edgeNameToIsCovered = new HashMap<>();

    Set<String> uncoveredVertexNames = new HashSet<>();
    Set<String> optionalEdgeNameSet = new HashSet<>();

    public List<Clique> getEdgeCliqueCover(Graph graph, List<Edge> optionalEdges, HeuristicType heuristicType) {

        initialiseHeuristicDataStructures(graph, optionalEdges);

        int cliqueNumber = -1;
        int maxCliqueSize = 0;
        int currentCliqueSize = 0;

        while (!uncoveredVertexNames.isEmpty()) {
            String i = selectVertex(heuristicType);

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
                    String edgeName = getEdgeName(i, j);
                    boolean isCovered = edgeNameToIsCovered.get(edgeName);
                    vertexNameToLocalUncoveredDegree.put(j, 1 - (isCovered ? 1 : 0));
                }

                String u = argMax(vertexNameToLocalUncoveredDegree, localNeighbourhoodOfi);
                while (vertexNameToLocalUncoveredDegree.get(u) > 0) {
                    boolean isOptional = true;

                    for (String j : finalCliques.get(cliqueNumber).getVertexNames()) {
                        String edgeNameUj = getEdgeName(u, j);
                        String edgeNameJi = getEdgeName(j, i);

                        if (!edgeNameToIsCovered.get(edgeNameUj)) {
                            edgeNameToIsCovered.replace(edgeNameUj, true);
                            vertexNameToUncoveredDegree.merge(u, -1, Integer::sum);
                            vertexNameToUncoveredDegree.merge(j, -1, Integer::sum);
                        }

                        finalCliques.get(cliqueNumber).addEdgeName(edgeNameUj);
                        finalCliques.get(cliqueNumber).addEdgeName(edgeNameJi);

                        edgeNameToNoOfCliquesItsContainedIn.merge(edgeNameUj, 1, Integer::sum);
                        if (!optionalEdgeNameSet.contains(edgeNameUj)) isOptional = false;
                    }

                    if (!isOptional) finalCliques.get(cliqueNumber).addVertexName(u);

                    currentCliqueSize += 1;
                    localNeighbourhoodOfi.retainAll(vertexNameToAllNeighbours.get(u));

                    for (String j : localNeighbourhoodOfi) {
                        String edgeName = getEdgeName(u, j);
                        if (!edgeNameToIsCovered.get(edgeName)) {
                            vertexNameToLocalUncoveredDegree.merge(j, 1, Integer::sum);
                        }
                    }

                    u = argMax(vertexNameToLocalUncoveredDegree, localNeighbourhoodOfi);
                    if (localNeighbourhoodOfi.isEmpty()) break;
                }
            }
            uncoveredVertexNames = updateUncoveredVertices();
        }

        handleNonMaximalCliques(maxCliqueSize);
        removeCliquesConsistingOfOptionalEdges();
        removeRedundantCliques();

        return finalCliques;
    }

    private void initialiseHeuristicDataStructures(Graph graph, List<Edge> optionalEdges) {
        for (Edge edge : graph.getEdges()) {
            vertexNameToAllNeighbours.computeIfAbsent(edge.getFirstVertexName(), k -> new HashSet<>())
                    .add(edge.getSecondVertexName());
            vertexNameToAllNeighbours.computeIfAbsent(edge.getSecondVertexName(), k -> new HashSet<>())
                    .add(edge.getFirstVertexName());
        }

        for (Edge edge : optionalEdges) {
            optionalEdgeNameSet.add(getEdgeName(edge.getFirstVertexName(), edge.getSecondVertexName()));
        }

        for (Edge edge : graph.getEdges()) {
            String edgeName = getEdgeName(edge.getFirstVertexName(), edge.getSecondVertexName());

            edgeNameToIsCovered.put(edgeName, false);
            edgeNameToNoOfCliquesItsContainedIn.put(edgeName, 0);
        }

        for (String vertexName : graph.getVertexNames()) {
            Set<String> neighbours = vertexNameToAllNeighbours.get(vertexName);
            if (neighbours != null && !neighbours.isEmpty()) {
                vertexNameToUncoveredDegree.put(vertexName, neighbours.size());
                uncoveredVertexNames.add(vertexName);
            } else {
                vertexNameToUncoveredDegree.put(vertexName, 0);
            }
        }
    }

    private void removeRedundantCliques() {
        Iterator<Clique> iterator = finalCliques.iterator();
        while (iterator.hasNext()) {
            Clique clique = iterator.next();
            if (isCliqueRedundant(clique.getEdgeNames())) {
                for (String edge : clique.getEdgeNames()) {
                    edgeNameToNoOfCliquesItsContainedIn.merge(edge, -1, Integer::sum);
                }
                iterator.remove();
            }
        }
    }

    private void removeCliquesConsistingOfOptionalEdges() {
        List<Clique> updatedCliques = finalCliques.stream().map(finalClique -> {
            boolean containsOnlyOptionalEdges = optionalEdgeNameSet.containsAll(finalClique.getEdgeNames());
            return containsOnlyOptionalEdges ? new Clique() : finalClique;
        }).toList();

        finalCliques.clear();
        finalCliques.addAll(updatedCliques);
    }

    private void handleNonMaximalCliques(int maxCliqueSize) {
        int currentCliqueIndex = 0;

        for (Clique clique : finalCliques) {
            if (clique.getVertexNames().size() < maxCliqueSize && !clique.getVertexNames().isEmpty()) {
                var firstClique = clique.getVertexNames().get(0);
                var firstCliqueNeighbours = vertexNameToAllNeighbours.get(firstClique);

                Set<String> neighboursOfFirstVertex = new HashSet<>(firstCliqueNeighbours);
                List<String> verticesToBeAdded = new ArrayList<>(neighboursOfFirstVertex);

                for (int x = 1; x < clique.getVertexNames().size(); x++) {
                    var currentClique = clique.getVertexNames().get(x);
                    var currentCliqueNeighbours = vertexNameToAllNeighbours.get(currentClique);
                    verticesToBeAdded.retainAll(currentCliqueNeighbours);
                }

                while (!verticesToBeAdded.isEmpty()) {
                    String i = verticesToBeAdded.get(0);
                    clique.addVertexName(i);
                    var neighboursOfi = vertexNameToAllNeighbours.get(i);
                    verticesToBeAdded.retainAll(neighboursOfi);

                    for (String s : clique.getVertexNames()) {
                        if (!i.equals(s)) {
                            String edgeName = getEdgeName(i, s);
                            finalCliques.get(currentCliqueIndex).addEdgeName(edgeName);
                            edgeNameToNoOfCliquesItsContainedIn.merge(edgeName, 1, Integer::sum);
                        }
                    }
                }
            }
            currentCliqueIndex++;
        }
    }

    private String argMin(Map<String, Integer> uncoveredDegree, Set<String> uncoveredVertices) {
        return uncoveredVertices.stream()
                .min(Comparator.comparingInt(uncoveredDegree::get))
                .orElse(null);
    }

    private String argMax(Map<String, Integer> uncoveredDegree, Set<String> uncoveredVertices) {
        return uncoveredVertices.stream()
                .max(Comparator.comparingInt(uncoveredDegree::get))
                .orElse(null);
    }

    private boolean isCliqueRedundant(List<String> edgeNames) {
        return edgeNames.stream()
                .allMatch(edge -> edgeNameToNoOfCliquesItsContainedIn.getOrDefault(edge, 0) > 1);
    }

    private Set<String> updateUncoveredVertices() {
        return uncoveredVertexNames.stream()
                .filter(v -> vertexNameToUncoveredDegree.get(v) > 0)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    private String getEdgeName(String vertex1, String vertex2) {
        return (vertex1.compareTo(vertex2) < 0)
                ? vertex1 + vertex2
                : vertex2 + vertex1;
    }

    private String selectVertex(HeuristicType heuristicType) {
        if (HeuristicType.MAXIMAL.equals(heuristicType)) {
            return argMax(vertexNameToUncoveredDegree, uncoveredVertexNames);
        } else if (HeuristicType.MINIMAL.equals(heuristicType)) {
            return argMin(vertexNameToUncoveredDegree, uncoveredVertexNames);
        } else if (HeuristicType.SEQUENCE.equals(heuristicType)) {
            return uncoveredVertexNames.iterator().next();
        }
        throw new IllegalArgumentException("Unsupported heuristic type: " + heuristicType);
    }
}
