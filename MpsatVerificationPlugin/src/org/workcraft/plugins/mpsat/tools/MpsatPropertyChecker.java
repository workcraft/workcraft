package org.workcraft.plugins.mpsat.tools;

import org.workcraft.VerificationTool;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class MpsatPropertyChecker extends VerificationTool {

    @Override
    public String getDisplayName() {
        return "Custom properties [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

}
