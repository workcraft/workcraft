package org.workcraft.plugins.cflt.tools;

import org.workcraft.workspace.WorkspaceEntry;

public interface VisualModelDrawingTool {

    /**
     * Renders a graph based on a precomputed rendering request.
     * The request contains all necessary structural information
     * (cliques, vertices, etc.) prepared by GraphInterpreterTool.
     */
    void renderGraph(RenderGraphRequest request);

    void drawSingleTransition(String name, WorkspaceEntry we);
}