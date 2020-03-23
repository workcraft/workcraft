package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.DialogUtils;
import org.workcraft.types.Pair;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.LinkedList;

public class OutputPersistencyVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Output persistency (without dummies) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
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
        if (!MpsatUtils.mutexStructuralCheck(stg, true)) {
            return false;
        }
        return true;
    }

    @Override
    public VerificationParameters getVerificationParameters(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Pair<String, String>> exceptions = MutexUtils.getMutexGrantPairs(stg);
        return ReachUtils.getOutputPersistencySettings(exceptions);
    }

}
