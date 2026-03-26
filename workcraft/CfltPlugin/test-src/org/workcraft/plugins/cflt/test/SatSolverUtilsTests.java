package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.utils.SatSolverUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SatSolverUtilsTests {

    @Test
    void encodeForEdgeCliqueCoverEncodesThreeVertexChainCorrectly() throws Exception {

        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");
        Vertex c = new Vertex("C");

        Graph graph = new Graph();

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);

        graph.addEdge(new Edge(a, b)); // e₀
        graph.addEdge(new Edge(b, c)); // e₁

        ISolver solver = mock(ISolver.class);

        SatSolverUtils.encodeForEdgeCliqueCover(
                graph,
                1,
                Set.of(),
                solver
        );

        ArgumentCaptor<VecInt> captor = ArgumentCaptor.forClass(VecInt.class);

        verify(solver, times(7)).addClause(captor.capture());

        List<VecInt> clauses = captor.getAllValues();

        assertEquals(7, clauses.size());

        // Non-adjacent vertices cannot be in the same clique
        // (-1 ∨ -3) meaning (¬A ∨ ¬C)
        assertArrayEquals(new int[]{-1, -3}, clauses.get(0).toArray());

        // If an edge is in the clique, both its vertices must also be in that clique
        // and the edge must be included in a clique

        // (-4 ∨ 1) meaning (¬e₀ ∨ A)
        assertArrayEquals(new int[]{-4, 1}, clauses.get(1).toArray());
        // (-4 ∨ 2) meaning (¬e₀ ∨ B)
        assertArrayEquals(new int[]{-4, 2}, clauses.get(2).toArray());

        // (4) meaning (e₀)
        assertArrayEquals(new int[]{4}, clauses.get(3).toArray());

        // (-5 ∨ 2) meaning (¬e₁ ∨ B)
        assertArrayEquals(new int[]{-5, 2}, clauses.get(4).toArray());
        // (-5 ∨ 3) meaning (¬e₁ ∨ C)
        assertArrayEquals(new int[]{-5, 3}, clauses.get(5).toArray());
        // (5) meaning (e₁)
        assertArrayEquals(new int[]{5}, clauses.get(6).toArray());
    }

    @Test
    void extractCliquesExtractsThreeVertexChainCorrectly() {

        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");
        Vertex c = new Vertex("C");

        Graph graph = new Graph();

        graph.addVertex(a);
        graph.addVertex(c);
        graph.addVertex(b);

        graph.addEdge(new Edge(a, b));
        graph.addEdge(new Edge(b, c));

        int maxCliques = 2;

        int[] model = {-1, 2, 3, -4, 5, 6};

        List<Clique> cliques = SatSolverUtils.extractCliques(
                graph,
                model,
                maxCliques
        );

        assertEquals(maxCliques, cliques.size());

        assertEquals(List.of(c, b), cliques.get(0).getVertices());
        assertEquals(List.of(new Edge(c, b)), cliques.get(0).getEdges());

        assertEquals(List.of(a, b), cliques.get(1).getVertices());
        assertEquals(List.of(new Edge(b, a)), cliques.get(1).getEdges());
    }
}