package org.workcraft.plugins.cflt.tools;

import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.presets.ExpressionParameters;
import org.workcraft.workspace.WorkspaceEntry;

public record DrawVisualObjectsRequest(
        Graph inputGraph,
        Graph outputGraph,
        boolean isSequence,
        boolean isRoot,
        ExpressionParameters.Mode mode,
        WorkspaceEntry workspaceEntry
) { }