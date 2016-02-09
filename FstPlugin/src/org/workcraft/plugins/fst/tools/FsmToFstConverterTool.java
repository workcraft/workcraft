package org.workcraft.plugins.fst.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class FsmToFstConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Finite State Transducer";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        MathModel mathModel = we.getModelEntry().getMathModel();
        return mathModel.getClass().equals(Fsm.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        we.captureMemento();
        try {
            final VisualFsm src = (VisualFsm)we.getModelEntry().getVisualModel();
            final VisualFst dst = new VisualFst(new Fst());
            final FsmToFstConverter converter = new FsmToFstConverter(src, dst);
            final Framework framework = Framework.getInstance();
            final Workspace workspace = framework.getWorkspace();
            final Path<String> directory = we.getWorkspacePath().getParent();
            final String name = we.getWorkspacePath().getNode();
            final ModelEntry me = new ModelEntry(new FsmDescriptor(), converter.getDstModel());
            boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());
            workspace.add(directory, name, me, false, openInEditor);
        } finally {
            we.cancelMemento();
        }
    }

}
