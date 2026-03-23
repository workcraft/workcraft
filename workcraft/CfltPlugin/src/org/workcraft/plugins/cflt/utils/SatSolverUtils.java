package org.workcraft.plugins.cflt.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;

public class SatSolverUtils {

    public static List<Clique> extractCliques(
            Graph graph,
            int[] model,
            int maxNumberOfCliques) {

        List<Clique> cliques = new ArrayList<>(maxNumberOfCliques);
        List<Vertex> vertices = graph.getVertices();

        for (int i = 0; i < maxNumberOfCliques; i++) {
            cliques.add(new Clique());
        }

        // Assign vertices to cliques
        for (int vertexIndex = 0; vertexIndex < vertices.size(); vertexIndex++) {

            for (int cliqueIndex = 0; cliqueIndex < maxNumberOfCliques; cliqueIndex++) {

                int var = getSatSolverVariable(
                        vertexIndex,
                        cliqueIndex,
                        maxNumberOfCliques
                );

                if (model[var - 1] > 0) {
                    cliques.get(cliqueIndex).addVertex(vertices.get(vertexIndex));
                }
            }
        }

        Map<Vertex, Set<Vertex>> vertexToNeighbours = graph.getVertexToAllNeighbours();

        // Build edges for each clique
        for (Clique clique : cliques) {

            HashSet<Vertex> cliqueVertices = new HashSet<>(clique.getVertices());

            for (Vertex v : cliqueVertices) {

                for (Vertex neighbour : vertexToNeighbours.get(v)) {

                    // ensure neighbour is also in clique
                    if (!cliqueVertices.contains(neighbour)) continue;

                    // enforce ordering to avoid duplicates
                    if (v.hashCode() >= neighbour.hashCode()) continue;

                    clique.addEdge(new Edge(v, neighbour));
                }
            }
        }

        return cliques;
    }

    /*
        Adds clauses (ANDs) made up of integer literals (ORs) to the solver.
        The sign of each integer denotes its truth assignment (+/- = true/false).

        1) Vertices that are not connected by an edge cannot be in the same clique.
        2) Each edge must be included in at least one clique.
    */
    public static void encodeForEdgeCliqueCover(
            Graph graph,
            int maxNumberOfCliques,
            Set<Edge> optionalEdges,
            ISolver solver) {

        List<Vertex> vertices = graph.getVertices();

        Map<Vertex, Integer> vertexToIndexMap = new HashMap<>();
        Map<Vertex, Set<Vertex>> vertexToAllNeighbours = graph.getVertexToAllNeighbours();

        for (int i = 0; i < vertices.size(); i++) {
            vertexToIndexMap.put(vertices.get(i), i);
        }

        // Adding clique validity constraints
        for (int x = 0; x < vertices.size(); x++) {

            // + 1 because we don't want to compare same vertex pairs
            for (int y = x + 1; y < vertices.size(); y++) {

                Vertex firstVertex = vertices.get(x);
                Vertex secondVertex = vertices.get(y);

                // Vertices which aren't connected by an edge can't be in the same clique
                if (!vertexToAllNeighbours.get(firstVertex).contains(secondVertex)) {

                    for (int i = 0; i < maxNumberOfCliques; i++) {

                        // - means the expression must be false
                        addClauseSafe(solver, new VecInt(new int[]{
                                -getSatSolverVariable(x, i, maxNumberOfCliques),
                                -getSatSolverVariable(y, i, maxNumberOfCliques),
                        }));

                    }
                }
            }
        }

        // Adding constrains for all edges to be contained in at least one clique
        int edgeIndex = 0;

        for (Edge edge : graph.getEdges()) {

            // Skip optional edges
            if (optionalEdges.contains(edge)) continue;

            int firstVertexIndex = vertexToIndexMap.get(edge.firstVertex());
            int secondVertexIndex = vertexToIndexMap.get(edge.secondVertex());

            VecInt coverageClause = new VecInt(maxNumberOfCliques);

            for (int i = 0; i < maxNumberOfCliques; i++) {

                int edgeCliqueVar = getEdgeCliqueVariable(edgeIndex, i, maxNumberOfCliques, vertices.size());

                coverageClause.push(edgeCliqueVar);

                // Either the edge is NOT assigned to clique i OR the vertex IS in clique i
                addClauseSafe(solver, new VecInt(new int[]{
                        -edgeCliqueVar,
                        getSatSolverVariable(firstVertexIndex, i, maxNumberOfCliques),
                }));

                addClauseSafe(solver, new VecInt(new int[]{
                        -edgeCliqueVar,
                        getSatSolverVariable(secondVertexIndex, i, maxNumberOfCliques),
                }));
            }

            addClauseSafe(solver, coverageClause);

            edgeIndex++;
        }
    }

    private static void addClauseSafe(ISolver solver, VecInt vector) {
        try {
            solver.addClause(vector);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    // + 1 because sat solver doesn't allow 0
    private static int getSatSolverVariable(
            int vertexIndex,
            int cliqueIndex,
            int maxNumberOfCliques) {

        return vertexIndex * maxNumberOfCliques + cliqueIndex + 1;
    }

    private static int getEdgeCliqueVariable(
            int edgeIndex,
            int cliqueIndex,
            int maxCliques,
            int vertexNumber) {

        int vertexVarCount = vertexNumber * maxCliques;

        return vertexVarCount + edgeIndex * maxCliques + cliqueIndex + 1;
    }
}