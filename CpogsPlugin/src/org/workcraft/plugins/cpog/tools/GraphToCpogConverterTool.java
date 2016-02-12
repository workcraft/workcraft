package org.workcraft.plugins.cpog.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.cpog.CPOG;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphToCpogConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Conditional Partial Order Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        MathModel mathModel = we.getModelEntry().getMathModel();
        return mathModel.getClass().equals(Graph.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        we.captureMemento();
        try {
            final VisualGraph graph = (VisualGraph) we.getModelEntry().getVisualModel();
            final VisualCPOG cpog = new VisualCPOG(new CPOG());
            final GraphToCpogConverter converter = new GraphToCpogConverter(graph, cpog);
            final Framework framework = Framework.getInstance();
            final Workspace workspace = framework.getWorkspace();
            final Path<String> directory = we.getWorkspacePath().getParent();
            final String name = we.getWorkspacePath().getNode();
            final ModelEntry me = new ModelEntry(new CpogDescriptor(), converter.getDstModel());
            boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
            workspace.add(directory, name, me, false, openInEditor);
        } finally {
            we.cancelMemento();
        }
    }

}
