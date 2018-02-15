package org.workcraft.plugins.petrify.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.interop.SgImporter;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class WriteSgConversionTask implements Task<WriteSgConversionResult> {

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

            hugeSgConfirmed = DialogUtils.showConfirmWarning(msg, "Building state graph", false);
        }

        public boolean isHugeSgConfirmed() {
            return hugeSgConfirmed;
        }
    }

    private final WorkspaceEntry we;
    private final boolean binary;
    private final Pattern hugeSgPattern = Pattern.compile("Do you really want to dump a state graph with ([0-9]+) states ?");

    public WriteSgConversionTask(WorkspaceEntry we, boolean binary) {
        this.we = we;
        this.binary = binary;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    @Override
    public Result<? extends WriteSgConversionResult> run(ProgressMonitor<? super WriteSgConversionResult> monitor) {
        final Framework framework = Framework.getInstance();
        try {
            // Common variables
            monitor.progressUpdate(0.05);
            PetriNetModel petri = WorkspaceUtils.getAs(we, PetriNetModel.class);
            Exporter petriExporter = Export.chooseBestExporter(framework.getPluginManager(), petri, StgFormat.getInstance());
            if (petriExporter == null) {
                throw new RuntimeException("Exporter not available: model class " + petri.getClass().getName() + " to format STG.");
            }
            SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);
            monitor.progressUpdate(0.10);

            // Generating .g file for Petri Net
            File petriFile = FileUtils.createTempFile("stg-", ".g");
            petriFile.deleteOnExit();
            ExportTask petriExportTask = new ExportTask(petriExporter, petri, petriFile.getAbsolutePath());
            Result<? extends Object> petriExportResult = framework.getTaskManager().execute(
                    petriExportTask, "Exporting .g", subtaskMonitor);

            if (petriExportResult.getOutcome() != Outcome.SUCCESS) {
                if (petriExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<WriteSgConversionResult>(Outcome.CANCEL);
                }
                return new Result<WriteSgConversionResult>(Outcome.FAILURE);
            }
            monitor.progressUpdate(0.20);

            // Generate State Graph
            List<String> writeSgOptions = new ArrayList<>();
            if (binary) {
                writeSgOptions.add("-bin");
            }

            while (true) {
                WriteSgTask writeSgTask = new WriteSgTask(writeSgOptions, petriFile, null, null);
                Result<? extends ExternalProcessOutput> writeSgResult = framework.getTaskManager().execute(
                        writeSgTask, "Building state graph", subtaskMonitor);

                if (writeSgResult.getOutcome() == Outcome.SUCCESS) {
                    try {
                        ByteArrayInputStream in = new ByteArrayInputStream(writeSgResult.getPayload().getStdout());
                        final Fst fst = new SgImporter().importSG(in);
                        return Result.success(new WriteSgConversionResult(null, fst));
                    } catch (DeserialisationException e) {
                        return Result.exception(e);
                    }
                }
                if (writeSgResult.getOutcome() == Outcome.CANCEL) {
                    return Result.cancelation();
                }
                if (writeSgResult.getCause() != null) {
                    return Result.exception(writeSgResult.getCause());
                } else {
                    final String errorMessages = new String(writeSgResult.getPayload().getStderr());
                    final Matcher matcher = hugeSgPattern.matcher(errorMessages);
                    if (matcher.find()) {
                        final HugeSgRunnable hugeSgRunnable = new HugeSgRunnable(matcher.group(1));
                        SwingUtilities.invokeAndWait(hugeSgRunnable);
                        if (hugeSgRunnable.isHugeSgConfirmed()) {
                            writeSgOptions.add("-huge");
                            continue;
                        } else {
                            return Result.cancelation();
                        }
                    } else {
                        return Result.failure(new WriteSgConversionResult(writeSgResult, null));
                    }
                }
            }
        } catch (Throwable e) {
            return new Result<WriteSgConversionResult>(e);
        }
    }

}
