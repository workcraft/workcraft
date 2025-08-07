package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.LinkedList;

public class OutputPersistencyVerificationCommand
        extends AbstractEssentialVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Output persistency (without dummies)";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public boolean checkPrerequisites(WorkspaceEntry we) {
        if (!super.checkPrerequisites(we)) {
            return false;
        }
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        if (!stg.getDummyTransitions().isEmpty()) {
            DialogUtils.showError("Output persistency can currently be checked only for STGs without dummies.");
            return false;
        }
        return MutexUtils.mutexStructuralCheck(stg, true);
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Pair<String, String>> exceptions = MutexUtils.getMutexGrantPersistencyExceptions(stg);
        return ReachUtils.getOutputPersistencyParameters(exceptions);
    }

}
