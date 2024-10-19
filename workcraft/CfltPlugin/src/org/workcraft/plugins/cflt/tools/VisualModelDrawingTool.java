package org.workcraft.plugins.cflt.tools;

import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.presets.ExpressionParameters;
import org.workcraft.workspace.WorkspaceEntry;

interface VisualModelDrawingTool {
    /**
     * Adds VisualObjects (eg. Visual Place/ Transitions)
     * to VisualModels (eg. VisualPetri/ VisualStg)
     * Relies on Edge Clique Cover Algorithms
     */
    void drawVisualObjects(
            Graph inputGraph, Graph outputGraph,
            boolean isSequence, boolean isRoot,
            ExpressionParameters.Mode mode, WorkspaceEntry we);
    void drawSingleTransition(String name, WorkspaceEntry we);
}
