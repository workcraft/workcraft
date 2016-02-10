package org.workcraft.plugins.petrify.tools;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petrify.tasks.DrawSgResult;
import org.workcraft.plugins.petrify.tasks.DrawSgTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ShowSg implements Tool {
    private final boolean binary;

    public ShowSg(boolean binary) {
        this.binary = binary;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, PetriNetModel.class);
    }

    @Override
    public String getSection() {
        return "External visualiser";
    }

    @Override
    public String getDisplayName() {
        return binary ? "State graph (binary-encoded) [write_sg + draw_astg]" : "State graph (basic) [write_sg + draw_astg]";
    }

    @Override
    public void run(WorkspaceEntry we) {
        DrawSgTask task = new DrawSgTask(we, binary);
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
                    DesktopApi.open(result.getReturnValue().getPsFile());
                    //SystemOpen.open(result.getReturnValue().getPsFile());
                } else  if (result.getOutcome() != Outcome.CANCELLED) {
                    String errorMessage = "Petrify tool chain execution failed :-(";
                    Throwable cause = result.getCause();
                    if (cause != null) {
                        errorMessage += "\n\nFailure caused by: " + cause.toString() + "\nPlease see the 'Problems' tab for more details.";
                    } else {
                        errorMessage += "\n\nFailure caused by: \n" + result.getReturnValue().getErrorMessages();
                    }
                    final String err = errorMessage;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null, err, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            }
        };

        framework.getTaskManager().queue(task, "Show state graph", monitor);
    }

}
