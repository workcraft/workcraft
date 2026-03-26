package org.workcraft.plugins.cflt.algorithms;

import java.util.List;
import java.util.Set;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.utils.CliqueUtils;
import org.workcraft.plugins.cflt.utils.SatSolverUtils;

public class EdgeCliqueCoverSolver {

    public static List<Clique> getEdgeCliqueCover(
            Graph graph,
            Set<Edge> optionalEdges) {

        List<Vertex> vertices = graph.getVertices();

        int maxNumberOfCliques = 1;

        while (maxNumberOfCliques <= vertices.size()) {

            ISolver solver = SolverFactory.newDefault();

            // pre-define max number of variables
            int noOfNonOptionalEdges = graph.getEdges().size() - optionalEdges.size();
            solver.newVar(maxNumberOfCliques * (vertices.size() + noOfNonOptionalEdges));

            SatSolverUtils.encodeForEdgeCliqueCover(
                    graph,
                    maxNumberOfCliques,
                    optionalEdges,
                    solver
            );

            boolean isSatisfiable = false;

            try {
                isSatisfiable = solver.isSatisfiable();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

            if (isSatisfiable) {
                int[] model = solver.model();

                List<Clique> cliques = SatSolverUtils.extractCliques(
                        graph,
                        model,
                        maxNumberOfCliques
                );

                int maxSize = cliques.stream()
                        .mapToInt(c -> c.getVertices().size())
                        .max()
                        .orElse(0);

                CliqueUtils.expandNonMaximalCliques(
                        maxSize,
                        cliques,
                        graph.getVertexToAllNeighbours(),
                        null);

                return cliques;
            }

            maxNumberOfCliques++;
        }

        throw new IllegalStateException(
                "SAT solver failed to find an edge clique cover."
        );
    }
}