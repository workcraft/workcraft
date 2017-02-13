package org.workcraft.plugins.wtg.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ParseWtgConversionTask implements Task<ParseWtgConversionResult> {

    private final WorkspaceEntry we;

    public ParseWtgConversionTask(WorkspaceEntry we) {
        this.we = we;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    @Override
    public Result<? extends ParseWtgConversionResult> run(ProgressMonitor<? super ParseWtgConversionResult> monitor) {
        final Framework framework = Framework.getInstance();
        try {
            // Common variables
            monitor.progressUpdate(0.05);
            Wtg wtg = WorkspaceUtils.getAs(we, Wtg.class);
            Exporter wtgExporter = Export.chooseBestExporter(framework.getPluginManager(), wtg, Format.WTG);
            if (wtgExporter == null) {
                throw new RuntimeException("Exporter not available: model class " + wtg.getClass().getName() + " to format STG.");
            }
            SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);
            monitor.progressUpdate(0.10);

            // Generating .wtg file
            File wtgFile = FileUtils.createTempFile("wtg-", ".g");
            wtgFile.deleteOnExit();
            ExportTask wtgExportTask = new ExportTask(wtgExporter, wtg, wtgFile.getAbsolutePath());
            Result<? extends Object> wtgExportResult = framework.getTaskManager().execute(
                    wtgExportTask, "Exporting .wtg", subtaskMonitor);

            if (wtgExportResult.getOutcome() != Outcome.FINISHED) {
                if (wtgExportResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<ParseWtgConversionResult>(Outcome.CANCELLED);
                }
                return new Result<ParseWtgConversionResult>(Outcome.FAILED);
            }
            monitor.progressUpdate(0.20);

            // Generate STG
            ParseWtgTask parseWtgTask = new ParseWtgTask(null, wtgFile, null, null);
            Result<? extends ExternalProcessResult> parseWtgResult = framework.getTaskManager().execute(
                    parseWtgTask, "Building state graph", subtaskMonitor);

            if (parseWtgResult.getOutcome() == Outcome.FINISHED) {
                try {
                    ByteArrayInputStream in = new ByteArrayInputStream(parseWtgResult.getReturnValue().getOutput());
                    final StgModel stg = new DotGImporter().importSTG(in);
                    return Result.finished(new ParseWtgConversionResult(null, (Stg) stg));
                } catch (DeserialisationException e) {
                    return Result.exception(e);
                }
            }
            if (parseWtgResult.getOutcome() == Outcome.CANCELLED) {
                return Result.cancelled();
            }
            if (parseWtgResult.getCause() != null) {
                return Result.exception(parseWtgResult.getCause());
            } else {
                return Result.failed(new ParseWtgConversionResult(parseWtgResult, null));
            }
        } catch (Throwable e) {
            return new Result<ParseWtgConversionResult>(e);
        }
    }

}
