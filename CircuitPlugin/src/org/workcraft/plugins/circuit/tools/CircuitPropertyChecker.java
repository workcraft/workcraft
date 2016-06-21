package org.workcraft.plugins.circuit.tools;

import org.workcraft.VerificationTool;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class CircuitPropertyChecker extends VerificationTool {

    @Override
    public String getDisplayName() {
        return "Custom properties [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Circuit;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

}
