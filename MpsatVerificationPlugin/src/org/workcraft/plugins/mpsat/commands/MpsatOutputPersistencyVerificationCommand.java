package org.workcraft.plugins.mpsat.commands;

import java.util.LinkedList;

import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatOutputPersistencyVerificationCommand extends MpsatAbstractVerificationCommand {

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
        boolean result = super.checkPrerequisites(we);
        if (result) {
            StgModel stg = WorkspaceUtils.getAs(we, StgModel.class);
            if (!stg.getDummyTransitions().isEmpty()) {
                DialogUtils.showError("Output persistency can currently be checked only for STGs without dummies.");
                result = false;
            }
        }
        return result;
    }
    @Override
    public MpsatParameters getSettings(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Pair<String, String>> exceptions = MutexUtils.getMutexGrantPairs(stg);
        return MpsatParameters.getOutputPersistencySettings(exceptions);
    }

}
