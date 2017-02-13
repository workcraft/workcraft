package org.workcraft.plugins.wtg.tasks;

import java.io.File;

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
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class WtgToStgConversionResultHandler extends DummyProgressMonitor<ParseWtgConversionResult> {

    private final ParseWtgConversionTask task;
    private WorkspaceEntry result;

    public WtgToStgConversionResultHandler(ParseWtgConversionTask task) {
        this.task = task;
        this.result = null;
    }

    @Override
    public void finished(final Result<? extends ParseWtgConversionResult> result, String description) {
        final Framework framework = Framework.getInstance();
        WorkspaceEntry we = task.getWorkspaceEntry();
        Path<String> path = we.getWorkspacePath();
        if (result.getOutcome() == Outcome.FINISHED) {
            Stg model = result.getReturnValue().getConversionResult();
            final Path<String> directory = path.getParent();
            final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
            final ModelEntry me = new ModelEntry(new StgDescriptor(), model);
            this.result = framework.createWork(me, directory, name);
        } else if (result.getOutcome() != Outcome.CANCELLED) {
            MainWindow mainWindow = framework.getMainWindow();
            if (result.getCause() == null) {
                Result<? extends ExternalProcessResult> petrifyResult = result.getReturnValue().getResult();
                JOptionPane.showMessageDialog(mainWindow,
                        "ParseWtg output:\n" + petrifyResult.getReturnValue().getErrorsHeadAndTail(),
                        "Conversion failed", JOptionPane.WARNING_MESSAGE);
            } else {
                ExceptionDialog.show(mainWindow, result.getCause());
            }
        }
    }

    public WorkspaceEntry getResult() {
        return result;
    }

}
