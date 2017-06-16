package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class DrawSgTask implements Task<DrawSgResult> {

    private static final String STG_FILE_NAME = "model.g";
    private static final String SG_FILE_NAME = "model.sg";
    private static final String RESULT_FILE_NAME = DesktopApi.getOs().isWindows() ? "model.ps" : "model.pdf";

    private final class HugeSgRunnable implements Runnable {
        private final String stateCountMsg;
        private boolean hugeSgConfirmed = false;

        private HugeSgRunnable(String stateCountMsg) {
            this.stateCountMsg = stateCountMsg;
        }

        @Override
        public void run() {
            String msg = "The state graph contains " + stateCountMsg + " states."
                    + "It may take a very long time to be processed.\n\n"
                    + "Are you sure you want to display it?";

            hugeSgConfirmed = DialogUtils.showConfirm(msg, "Please confirm");
        }

        public boolean isHugeSgConfirmed() {
            return hugeSgConfirmed;
        }
    }

    final Pattern hugeSgPattern = Pattern.compile("with ([0-9]+) states");

    private final WorkspaceEntry we;
    private final boolean binary;

    public DrawSgTask(WorkspaceEntry we, boolean binary) {
        this.we = we;
        this.binary = binary;
    }

    @Override
    public Result<? extends DrawSgResult> run(ProgressMonitor<? super DrawSgResult> monitor) {
        final Framework framework = Framework.getInstance();
        File directory = FileUtils.createTempDirectory();
        try {
            File stgFile = new File(directory, STG_FILE_NAME);
            stgFile.deleteOnExit();
            Model model = WorkspaceUtils.getAs(we, PetriNetModel.class);
            ExportTask exportTask = Export.createExportTask(model, stgFile, Format.STG, framework.getPluginManager());
            final Result<? extends Object> dotGResult = framework.getTaskManager().execute(exportTask, "Exporting to .g");

            if (dotGResult.getOutcome() != Outcome.FINISHED) {
                if (dotGResult.getOutcome() != Outcome.CANCELLED) {
                    if (dotGResult.getCause() != null) {
                        return Result.exception(dotGResult.getCause());
                    } else {
                        return Result.failed(new DrawSgResult(null, "Export to .g failed for unknown reason"));
                    }
                }
                return Result.cancelled();
            }

            File sgFile = new File(directory, SG_FILE_NAME);
            sgFile.deleteOnExit();
            List<String> writeSgOptions = new ArrayList<>();
            writeSgOptions.add("-write_sg");
            if (binary) {
                writeSgOptions.add("-bin");
            }
            while (true) {
                WriteSgTask writeSgTask = new WriteSgTask(writeSgOptions, stgFile, sgFile, directory);
                Result<? extends ExternalProcessResult> writeSgResult = framework.getTaskManager().execute(
                        writeSgTask, "Running Petrify");

                if (writeSgResult.getOutcome() == Outcome.FINISHED) {
                    break;
                }
                if (writeSgResult.getOutcome() == Outcome.CANCELLED) {
                    return Result.cancelled();
                }
                if (writeSgResult.getCause() != null) {
                    return Result.exception(writeSgResult.getCause());
                } else {
                    final String errorMessages = new String(writeSgResult.getReturnValue().getErrors());
                    final Matcher matcher = hugeSgPattern.matcher(errorMessages);
                    if (matcher.find()) {
                        final HugeSgRunnable hugeSgRunnable = new HugeSgRunnable(matcher.group(1));
                        SwingUtilities.invokeAndWait(hugeSgRunnable);
                        if (hugeSgRunnable.isHugeSgConfirmed()) {
                            writeSgOptions.add("-huge");
                            continue;
                        } else {
                            return Result.cancelled();
                        }
                    } else {
                        return Result.failed(new DrawSgResult(null, errorMessages));
                    }
                }
            }
            File resultFile = new File(directory, RESULT_FILE_NAME);
            resultFile.deleteOnExit();
            ArrayList<String> drawAstgOptions = new ArrayList<>();
            drawAstgOptions.add("-sg");
            if (binary) {
                drawAstgOptions.add("-bin");
            }
            DrawAstgTask drawAstgTask = new DrawAstgTask(drawAstgOptions, sgFile, resultFile, directory);
            final Result<? extends ExternalProcessResult> drawAstgResult = framework.getTaskManager().execute(drawAstgTask, "Running Petrify");

            if (drawAstgResult.getOutcome() != Outcome.FINISHED) {
                if (drawAstgResult.getOutcome() != Outcome.CANCELLED) {
                    if (drawAstgResult.getCause() != null) {
                        return Result.exception(drawAstgResult.getCause());
                    } else {
                        return Result.failed(new DrawSgResult(null, "Errors running Petrify:\n"
                            + new String(drawAstgResult.getReturnValue().getErrors())));
                    }
                }
                return Result.cancelled();
            }
            return Result.finished(new DrawSgResult(resultFile, "No errors"));
        } catch (Throwable e) {
            return Result.exception(e);
        }
    }

}
