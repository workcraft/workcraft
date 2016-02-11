package org.workcraft.plugins.policy.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriNetGeneratorTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof PolicyNet;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualPolicyNet visualModel = (VisualPolicyNet)we.getModelEntry().getVisualModel();
        final PetriNetGenerator generator = new PetriNetGenerator(visualModel);
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        final Path<String> directory = we.getWorkspacePath().getParent();
        final String desiredName = we.getWorkspacePath().getNode();
        final ModelEntry me = new ModelEntry(new PetriNetDescriptor(), generator.getPetriNet());
        boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
        workspace.add(directory, desiredName, me, false, openInEditor);
    }
}
