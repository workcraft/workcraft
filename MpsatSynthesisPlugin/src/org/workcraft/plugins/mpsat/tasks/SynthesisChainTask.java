package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.SynthesisParameters;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.Collection;

public class SynthesisChainTask implements Task<SynthesisChainOutput> {
    private final WorkspaceEntry we;
    private final SynthesisParameters settings;
    private final Collection<Mutex> mutexes;

    public SynthesisChainTask(WorkspaceEntry we, SynthesisParameters settings, Collection<Mutex> mutexes) {
        this.we = we;
        this.settings = settings;
        this.mutexes = mutexes;
    }

    @Override
    public Result<? extends SynthesisChainOutput> run(ProgressMonitor<? super SynthesisChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        StgFormat format = StgFormat.getInstance();
        String stgFileExtension = format.getExtension();
        try {
            Stg model = WorkspaceUtils.getAs(we, Stg.class);
            Exporter exporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), model, format);
            if (exporter == null) {
                throw new NoExporterException(model, format);
            }
            SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);

            // Generate .g for the model
            String filePrefix = StgUtils.SPEC_FILE_PREFIX;
            File netFile = new File(directory, filePrefix + stgFileExtension);
            ExportTask exportTask = new ExportTask(exporter, model, netFile.getAbsolutePath());
            Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(
                    exportTask, "Exporting .g", subtaskMonitor);

            if (exportResult.getOutcome() != Outcome.SUCCESS) {
                if (exportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new SynthesisChainOutput(exportResult, null, null, settings));
            }
            if ((mutexes != null) && !mutexes.isEmpty()) {
                model = StgUtils.loadStg(netFile);
                MutexUtils.factoroutMutexs(model, mutexes);
                filePrefix += StgUtils.MUTEX_FILE_SUFFIX;
                netFile = new File(directory, filePrefix + stgFileExtension);
                exportTask = new ExportTask(exporter, model, netFile.getAbsolutePath());
                exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");

                if (exportResult.getOutcome() != Outcome.SUCCESS) {
                    if (exportResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new SynthesisChainOutput(exportResult, null, null, settings));
                }
            }
            monitor.progressUpdate(0.33);

            // Generate unfolding
            File unfoldingFile = new File(directory, filePrefix + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(netFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = framework.getTaskManager().execute(punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new SynthesisChainOutput(exportResult, punfResult, null, settings));
            }
            monitor.progressUpdate(0.66);

            // Run MPSat on the generated unfolding
            boolean needsGateLibrary = settings.getMode().needLib();
            SynthesisTask mpsatTask = new SynthesisTask(settings.getMpsatArguments(directory),
                    unfoldingFile.getAbsolutePath(), directory, needsGateLibrary);
            Result<? extends SynthesisOutput> mpsatResult = framework.getTaskManager().execute(
                    mpsatTask, "Running synthesis [MPSat]", subtaskMonitor);

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new SynthesisChainOutput(exportResult, punfResult, mpsatResult, settings));
            }
            monitor.progressUpdate(1.0);

            return new Result<>(Outcome.SUCCESS,
                    new SynthesisChainOutput(exportResult, punfResult, mpsatResult, settings));
        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

}
