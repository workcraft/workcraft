package org.workcraft.plugins.pcomp.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class PcompResultHandlingMonitor extends AbstractResultHandlingMonitor<PcompOutput, WorkspaceEntry> {

    private final Collection<Mutex> mutexes = new ArrayList<>();

    public void setMutexes(Collection<Mutex> mutexes) {
        this.mutexes.addAll(mutexes);
    }

    @Override
    public WorkspaceEntry handle(final Result<? extends PcompOutput> pcompResult) {
        PcompOutput pcompOutput = pcompResult.getPayload();
        if (pcompResult.getOutcome() == Outcome.SUCCESS) {
            try {
                Framework framework = Framework.getInstance();
                File outputFile = pcompOutput.getOutputFile();
                WorkspaceEntry we = framework.loadWork(outputFile);
                StgModel model = WorkspaceUtils.getAs(we, StgModel.class);
                MutexUtils.restoreMutexPlacesByName(model, mutexes);
                return we;
            } catch (DeserialisationException e) {
                DialogUtils.showError(e.getMessage());
            }
        }

        if (pcompResult.getOutcome() == Outcome.FAILURE) {
            String message;
            if (pcompResult.getCause() != null) {
                message = pcompResult.getCause().toString();
            } else {
                message = "Pcomp errors:\n" + pcompOutput.getErrorsHeadAndTail();
            }
            DialogUtils.showError(message);
        }
        return null;
    }

}
