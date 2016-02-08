package org.workcraft.plugins.policy.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.PolicyNetDescriptor;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriNetToPolicyNetConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Policy Net";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return (we.getModelEntry().getMathModel() instanceof PetriNet);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualPetriNet srcModel = (VisualPetriNet)we.getModelEntry().getVisualModel();
        final VisualPolicyNet dstModel = new VisualPolicyNet(new PolicyNet());
        final PetriNetToPolicyNetConverter converter = new PetriNetToPolicyNetConverter(srcModel, dstModel);
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        final Path<String> directory = we.getWorkspacePath().getParent();
        final String desiredName = we.getWorkspacePath().getNode();
        final ModelEntry me = new ModelEntry(new PolicyNetDescriptor(), converter.getDstModel());
        boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());
        workspace.add(directory, desiredName, me, false, openInEditor);
    }
}
