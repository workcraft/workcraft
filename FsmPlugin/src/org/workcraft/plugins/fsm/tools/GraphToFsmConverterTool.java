package org.workcraft.plugins.fsm.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphToFsmConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Finite State Machine";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        MathModel mathModel = we.getModelEntry().getMathModel();
        return mathModel.getClass().equals(Graph.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualGraph graph = (VisualGraph) we.getModelEntry().getVisualModel();
        final VisualFsm fsm = new VisualFsm(new Fsm());
        final GraphToFsmConverter converter = new GraphToFsmConverter(graph, fsm);
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        final Path<String> directory = we.getWorkspacePath().getParent();
        final String desiredName = we.getWorkspacePath().getNode();
        final ModelEntry me = new ModelEntry(new FsmDescriptor(), converter.getDstModel());
        boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
        workspace.add(directory, desiredName, me, false, openInEditor);
    }
}
