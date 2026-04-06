package org.workcraft.plugins.cflt.tools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.utils.GraphUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphInterpreterTool {

    private final EdgeCliqueCoverTool edgeCliqueCoverTool;

    public GraphInterpreterTool(EdgeCliqueCoverTool edgeCliqueCoverTool) {
        this.edgeCliqueCoverTool = edgeCliqueCoverTool;
    }

    public RenderGraphRequest buildRenderRequest(
            Graph inputGraph,
            Graph outputGraph,
            boolean isSequence,
            boolean isRoot,
            Mode mode,
            WorkspaceEntry we
    ) {

        Graph initialGraph = isSequence
                ? GraphUtils.join(inputGraph, outputGraph)
                : inputGraph;

        Set<Edge> optionalEdges = isSequence
                ? new HashSet<>(inputGraph.getEdges())
                : new HashSet<>();

        List<Clique> cliques = edgeCliqueCoverTool.getEdgeCliqueCover(
                initialGraph,
                optionalEdges,
                mode
        );

        Set<Vertex> inputVertices = isSequence
                ? new HashSet<>(inputGraph.getVertices())
                : new HashSet<>();

        List<Vertex> isolatedVertices = inputGraph.getIsolatedVertices();

        return new RenderGraphRequest(
                cliques,
                inputVertices,
                isolatedVertices,
                isRoot,
                we
        );
    }
}