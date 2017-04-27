package org.workcraft.plugins.wtg.tasks;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class WtgToStgConversionResultHandler extends DummyProgressMonitor<WaverConversionResult> {

    private final WaverConversionTask task;
    private WorkspaceEntry result;

    public WtgToStgConversionResultHandler(final WaverConversionTask task) {
        this.task = task;
        this.result = null;
    }

    @Override
    public void finished(final Result<? extends WaverConversionResult> result, final String description) {
        final Framework framework = Framework.getInstance();
        if (result.getOutcome() == Outcome.FINISHED) {
            final Stg model = result.getReturnValue().getConversionResult();
            final ModelEntry me = new ModelEntry(new StgDescriptor(), model);
            final Path<String> path = task.getWorkspaceEntry().getWorkspacePath();
            this.result = framework.createWork(me, path);
        } else if (result.getOutcome() != Outcome.CANCELLED) {
            final MainWindow mainWindow = framework.getMainWindow();
            if (result.getCause() != null) {
                ExceptionDialog.show(mainWindow, result.getCause());
            } else {
                String message = "Unexpected Waver error";
                if (result.getReturnValue() != null) {
                    final Result<? extends ExternalProcessResult> waverResult = result.getReturnValue().getResult();
                    message = "Waver output:\n" + waverResult.getReturnValue().getErrorsHeadAndTail();
                }
                JOptionPane.showMessageDialog(mainWindow, message, "Conversion failed", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public WorkspaceEntry getResult() {
        return result;
    }

}
