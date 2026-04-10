package org.workcraft.plugins.cflt.tools;

import java.util.List;
import java.util.Set;

import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.workspace.WorkspaceEntry;

public record RenderGraphRequest(
        List<Clique> cliques,
        Set<Vertex> inputVertices,
        List<Vertex> isolatedVertices,
        boolean isRoot,
        WorkspaceEntry workspaceEntry
) { }