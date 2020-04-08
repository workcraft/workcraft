package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class NormalcyVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Normalcy [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        return new VerificationParameters("Normalcy",
                VerificationMode.NORMALCY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                null, true);
    }

}
