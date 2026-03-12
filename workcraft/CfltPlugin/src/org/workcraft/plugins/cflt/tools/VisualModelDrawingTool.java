package org.workcraft.plugins.cflt.tools;

import org.workcraft.workspace.WorkspaceEntry;

public interface VisualModelDrawingTool {

    /**
     * Adds VisualObjects (eg. Visual Place/Transitions)
     * to VisualModels (eg. VisualPetri/VisualStg)
     * Relies on Edge Clique Cover Algorithms
     */
    void drawVisualObjects(DrawVisualObjectsRequest request);

    void drawSingleTransition(String name, WorkspaceEntry we);
}