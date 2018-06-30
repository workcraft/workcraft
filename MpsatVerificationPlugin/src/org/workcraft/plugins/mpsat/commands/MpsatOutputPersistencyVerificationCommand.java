package org.workcraft.plugins.mpsat.commands;

import java.util.LinkedList;

import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.MutexUtils;
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
        return 2;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public MpsatParameters getSettings(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Pair<String, String>> exceptions = MutexUtils.getMutexGrantPairs(stg);
        return MpsatParameters.getOutputPersistencySettings(exceptions);
    }

}
