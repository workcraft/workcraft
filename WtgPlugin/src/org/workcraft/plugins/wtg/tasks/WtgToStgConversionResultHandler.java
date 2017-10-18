package org.workcraft.plugins.wtg.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.tasks.AbstractExtendedResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class WtgToStgConversionResultHandler extends AbstractExtendedResultHandler<WaverConversionResult, WorkspaceEntry> {

    private final WaverConversionTask task;

    public WtgToStgConversionResultHandler(final WaverConversionTask task) {
        this.task = task;
    }

    @Override
    public WorkspaceEntry handleResult(final Result<? extends WaverConversionResult> result) {
        WorkspaceEntry weResult = null;
        if (result.getOutcome() == Outcome.SUCCESS) {
            final Stg model = result.getReturnValue().getConversionResult();
            final ModelEntry me = new ModelEntry(new StgDescriptor(), model);
            final Path<String> path = task.getWorkspaceEntry().getWorkspacePath();
            final Framework framework = Framework.getInstance();
            weResult = framework.createWork(me, path);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            if (result.getCause() != null) {
                ExceptionDialog.show(result.getCause());
            } else {
                String message = "Unexpected Waver error";
                if (result.getReturnValue() != null) {
                    final Result<? extends ExternalProcessResult> waverResult = result.getReturnValue().getResult();
                    message = "Waver output:\n" + waverResult.getReturnValue().getErrorsHeadAndTail();
                }
                DialogUtils.showWarning(message);
            }
        }
        return weResult;
    }

}
