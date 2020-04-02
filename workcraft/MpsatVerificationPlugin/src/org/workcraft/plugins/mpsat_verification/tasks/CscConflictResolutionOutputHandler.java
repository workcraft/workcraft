package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.AbstractOutputInterpreter;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;

public class CscConflictResolutionOutputHandler extends AbstractOutputInterpreter<MpsatOutput, WorkspaceEntry> {

    private final Collection<Mutex> mutexes;

    public CscConflictResolutionOutputHandler(WorkspaceEntry we,
            MpsatOutput output, Collection<Mutex> mutexes, boolean interactive) {

        super(we, output, interactive);
        this.mutexes = mutexes;
    }

    @Override
    public WorkspaceEntry interpret() {
        if (getOutput() == null) {
            return null;
        }
        final StgModel model = getOutput().getOutputStg();
        if (model == null) {
            final String errorMessage = getOutput().getErrorsHeadAndTail();
            DialogUtils.showWarning("Conflict resolution failed. MPSat output: \n" + errorMessage);
            return null;
        }
        model.setTitle(getWorkspaceEntry().getModelTitle());
        MutexUtils.restoreMutexSignals(model, mutexes);
        MutexUtils.restoreMutexPlacesByName(model, mutexes);
        final ModelEntry me = new ModelEntry(new StgDescriptor(), model);
        final Path<String> path = getWorkspaceEntry().getWorkspacePath();
        return Framework.getInstance().createWork(me, path);
    }

}
