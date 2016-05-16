package org.workcraft.plugins.stg.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriToStgConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        MathModel mathModel = we.getModelEntry().getMathModel();
        return mathModel.getClass().equals(Stg.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualPetriNet petri = (VisualPetriNet) we.getModelEntry().getVisualModel();
        final VisualStg stg = new VisualStg(new Stg());
        final PetriToStgConverter converter = new PetriToStgConverter(petri, stg);
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        final Path<String> directory = we.getWorkspacePath().getParent();
        final String name = we.getWorkspacePath().getNode();
        final ModelEntry me = new ModelEntry(new StgDescriptor(), converter.getDstModel());
        boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
        workspace.add(directory, name, me, false, openInEditor);
    }

}
