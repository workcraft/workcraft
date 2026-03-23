package org.workcraft.plugins.cflt.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Vertex;

public class CliqueUtils {

    public static void expandNonMaximalCliques(
            int maxCliqueSize,
            List<Clique> cliquesToBeExpaned,
            Map<Vertex, Set<Vertex>> vertexToAllNeighbours,
            Map<Edge, Integer> edgeToNoOfCliquesItsContainedIn) {

        int currentCliqueIndex = 0;

        for (Clique clique : cliquesToBeExpaned) {
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
                            cliquesToBeExpaned.get(currentCliqueIndex).addEdge(edge);
                            if (edgeToNoOfCliquesItsContainedIn != null) {
                                edgeToNoOfCliquesItsContainedIn.merge(edge, 1, Integer::sum);
                            }
                        }
                    }
                }
            }
            currentCliqueIndex++;
        }
    }
}
