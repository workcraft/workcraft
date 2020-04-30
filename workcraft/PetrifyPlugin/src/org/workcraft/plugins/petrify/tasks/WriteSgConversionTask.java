package org.workcraft.plugins.petrify.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.utils.FstUtils;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.*;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteSgConversionTask implements Task<WriteSgConversionOutput> {

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

    @Override
    public Result<? extends WriteSgConversionOutput> run(ProgressMonitor<? super WriteSgConversionOutput> monitor) {
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);
        try {
            // Common variables
            monitor.progressUpdate(0.05);
            PetriModel petri = WorkspaceUtils.getAs(we, PetriModel.class);
            StgFormat format = StgFormat.getInstance();
            Exporter petriExporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), petri, format);
            if (petriExporter == null) {
                throw new NoExporterException(petri, format);
            }
            monitor.progressUpdate(0.10);

            // Generating .g file for Petri Net
            File petriFile = FileUtils.createTempFile("stg-", ".g");
            petriFile.deleteOnExit();
            ExportTask petriExportTask = new ExportTask(petriExporter, petri, petriFile);
            Result<? extends ExportOutput> petriExportResult = taskManager.execute(
                    petriExportTask, "Exporting .g", subtaskMonitor);

            if (!petriExportResult.isSuccess()) {
                if (petriExportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.exception(petriExportResult.getCause());
            }
            monitor.progressUpdate(0.20);

            // Generate State Graph
            List<String> writeSgOptions = new ArrayList<>();
            if (binary) {
                writeSgOptions.add("-bin");
            }

            while (true) {
                WriteSgTask writeSgTask = new WriteSgTask(writeSgOptions, petriFile, null, null);
                Result<? extends ExternalProcessOutput> result = taskManager.execute(
                        writeSgTask, "Building state graph", subtaskMonitor);

                ExternalProcessOutput output = result.getPayload();
                if (result.isSuccess()) {
                    Fst fst = FstUtils.importFst(output.getStdout());
                    return Result.success(new WriteSgConversionOutput(output, fst));
                }

                if (result.isCancel()) {
                    return Result.cancel();
                }

                if (result.getCause() != null) {
                    return Result.exception(result.getCause());
                }

                String errorMessages = output.getStderrString();
                Matcher matcher = hugeSgPattern.matcher(errorMessages);
                if (matcher.find()) {
                    HugeSgRunnable hugeSgRunnable = new HugeSgRunnable(matcher.group(1));
                    SwingUtilities.invokeAndWait(hugeSgRunnable);
                    if (hugeSgRunnable.isHugeSgConfirmed()) {
                        writeSgOptions.add("-huge");
                        continue;
                    } else {
                        return Result.cancel();
                    }
                } else {
                    return Result.failure(new WriteSgConversionOutput(output, null));
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
