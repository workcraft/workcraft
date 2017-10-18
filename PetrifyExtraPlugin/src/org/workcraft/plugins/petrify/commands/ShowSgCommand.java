package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petrify.tasks.DrawSgResult;
import org.workcraft.plugins.petrify.tasks.DrawSgTask;
import org.workcraft.tasks.BasicProgressMonitor;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ShowSgCommand implements Command {
    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public String getSection() {
        return "External visualiser";
    }

    @Override
    public String getDisplayName() {
        return isBinary() ? "State graph (binary-encoded) [Petrify]" : "State graph (basic) [Petrify]";
    }

    @Override
    public void run(WorkspaceEntry we) {
        DrawSgTask task = new DrawSgTask(we, isBinary());
        final Framework framework = Framework.getInstance();

        ProgressMonitor<DrawSgResult> monitor = new BasicProgressMonitor<DrawSgResult>() {
            @Override
            public void finished(Result<? extends DrawSgResult> result) {
                super.finished(result);
                if (result.getOutcome() == Outcome.SUCCESS) {
                    DesktopApi.open(result.getReturnValue().getFile());
                } else  if (result.getOutcome() != Outcome.CANCEL) {
                    String errorMessage = "Petrify tool chain execution failed.";
                    Throwable cause = result.getCause();
                    if (cause != null) {
                        errorMessage += ERROR_CAUSE_PREFIX + cause.toString();
                    } else {
                        DrawSgResult returnValue = result.getReturnValue();
                        if (returnValue != null) {
                            errorMessage += ERROR_CAUSE_PREFIX + returnValue.getErrorMessages();
                        }
                    }
                    DialogUtils.showError(errorMessage);
                }
            }
        };

        final TaskManager taskManager = framework.getTaskManager();
        taskManager.queue(task, "Show state graph", monitor);
    }

}
