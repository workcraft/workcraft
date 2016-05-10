package org.workcraft.plugins.pog.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.pog.Pog;
import org.workcraft.plugins.pog.VisualPog;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PogToPnConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        MathModel mathModel = we.getModelEntry().getMathModel();
        return mathModel.getClass().equals(Pog.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualPog pog = (VisualPog) we.getModelEntry().getVisualModel();
        final VisualPetriNet pn = new VisualPetriNet(new PetriNet());
        final PogToPnConverter converter = new PogToPnConverter(pog, pn);
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        final Path<String> directory = we.getWorkspacePath().getParent();
        final String desiredName = we.getWorkspacePath().getNode();
        final ModelEntry me = new ModelEntry(new PetriNetDescriptor(), converter.getDstModel());
        boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
        workspace.add(directory, desiredName, me, false, openInEditor);
    }
}
