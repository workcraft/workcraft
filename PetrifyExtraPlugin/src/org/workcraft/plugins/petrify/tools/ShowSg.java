package org.workcraft.plugins.petrify.tools;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petrify.tasks.DrawSgResult;
import org.workcraft.plugins.petrify.tasks.DrawSgTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ShowSg implements Tool {
    private static final String TITLE = "State graph synthesis";
    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, PetriNetModel.class);
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
    public ModelEntry run(ModelEntry me) {
        DrawSgTask task = new DrawSgTask(me, isBinary());
        final Framework framework = Framework.getInstance();

        ProgressMonitor<DrawSgResult> monitor = new ProgressMonitor<DrawSgResult>() {
            @Override
            public void progressUpdate(double completion) {
            }

            @Override
            public void stdout(byte[] data) {
            }

            @Override
            public void stderr(byte[] data) {
            }

            @Override
            public boolean isCancelRequested() {
                return false;
            }

            @Override
            public void finished(Result<? extends DrawSgResult> result, String description) {
                if (result.getOutcome() == Outcome.FINISHED) {
                    DesktopApi.open(result.getReturnValue().getFile());
                } else  if (result.getOutcome() != Outcome.CANCELLED) {
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
                    final MainWindow mainWindow = framework.getMainWindow();
                    JOptionPane.showMessageDialog(mainWindow, errorMessage, TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        final TaskManager taskManager = framework.getTaskManager();
        taskManager.queue(task, "Show state graph", monitor);
        return me;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        run(we.getModelEntry());
        return we;
    }

}
