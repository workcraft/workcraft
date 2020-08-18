package org.workcraft.plugins.pcomp.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.workspace.ModelEntry;
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
        if (pcompResult.isSuccess()) {
            File outputFile = pcompOutput.getOutputFile();
            Stg stg = StgUtils.importStg(outputFile);
            MutexUtils.restoreMutexPlacesByName(stg, mutexes);

            ModelEntry me = new ModelEntry(new StgDescriptor(), stg);
            String name = FileUtils.getFileNameWithoutExtension(outputFile);
            return Framework.getInstance().createWork(me, name);
        }

        if (pcompResult.isFailure()) {
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
