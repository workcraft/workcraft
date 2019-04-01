package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;

public class CscConflictResolutionOutputHandler implements Runnable {

    private final WorkspaceEntry we;
    private final VerificationOutput output;
    private final Collection<Mutex> mutexes;
    private WorkspaceEntry weResult = null;

    public CscConflictResolutionOutputHandler(final WorkspaceEntry we,
            VerificationOutput output, Collection<Mutex> mutexes) {
        this.we = we;
        this.output = output;
        this.mutexes = mutexes;
    }

    @Override
    public void run() {
        final Framework framework = Framework.getInstance();
        final StgModel model = output.getOutputStg();
        if (model == null) {
            final String errorMessage = output.getErrorsHeadAndTail();
            DialogUtils.showWarning("Conflict resolution failed. MPSat output: \n" + errorMessage);
        } else {
            MutexUtils.restoreMutexSignals(model, mutexes);
            MutexUtils.restoreMutexPlacesByName(model, mutexes);
            final ModelEntry me = new ModelEntry(new StgDescriptor(), model);
            final Path<String> path = we.getWorkspacePath();
            weResult = framework.createWork(me, path);
        }
    }

    public WorkspaceEntry getResult() {
        return weResult;
    }

}
