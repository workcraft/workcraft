package org.workcraft.plugins.cpog.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.cpog.Cpog;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class CpogToGraphConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Directed Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        MathModel mathModel = we.getModelEntry().getMathModel();
        return mathModel.getClass().equals(Cpog.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        we.captureMemento();
        try {
            final VisualCpog cpog = (VisualCpog) we.getModelEntry().getVisualModel();
            final VisualGraph graph = new VisualGraph(new Graph());
            final CpogToGraphConverter converter = new CpogToGraphConverter(cpog, graph);
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
