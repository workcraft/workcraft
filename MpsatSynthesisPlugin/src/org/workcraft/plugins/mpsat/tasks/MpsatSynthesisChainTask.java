package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.util.Collection;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.MpsatSynthesisParameters;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExportTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.ExportUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatSynthesisChainTask implements Task<MpsatSynthesisChainOutput> {
    private final WorkspaceEntry we;
    private final MpsatSynthesisParameters settings;
    private final Collection<Mutex> mutexes;

    public MpsatSynthesisChainTask(WorkspaceEntry we, MpsatSynthesisParameters settings, Collection<Mutex> mutexes) {
        this.we = we;
        this.settings = settings;
        this.mutexes = mutexes;
    }

    @Override
    public Result<? extends MpsatSynthesisChainOutput> run(ProgressMonitor<? super MpsatSynthesisChainOutput> monitor) {
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
                        new MpsatSynthesisChainOutput(exportResult, null, null, settings));
            }
            if (!mutexes.isEmpty()) {
                model = StgUtils.loadStg(netFile);
                for (Mutex m: mutexes) {
                    model.setSignalType(m.g1.name, Signal.Type.INPUT);
                    model.setSignalType(m.g2.name, Signal.Type.INPUT);
                }
                filePrefix += StgUtils.MUTEX_FILE_SUFFIX;
                netFile = new File(directory, filePrefix + stgFileExtension);
                exportTask = new ExportTask(exporter, model, netFile.getAbsolutePath());
                exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");

                if (exportResult.getOutcome() != Outcome.SUCCESS) {
                    if (exportResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new MpsatSynthesisChainOutput(exportResult, null, null, settings));
                }
            }
            monitor.progressUpdate(0.33);

            // Generate unfolding
            File unfoldingFile = new File(directory, filePrefix + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(netFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            Result<? extends PunfOutput> punfResult = framework.getTaskManager().execute(punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatSynthesisChainOutput(exportResult, punfResult, null, settings));
            }
            monitor.progressUpdate(0.66);

            // Run MPSat on the generated unfolding
            boolean needLib = settings.getMode().needLib();
            MpsatSynthesisTask mpsatTask = new MpsatSynthesisTask(settings.getMpsatArguments(directory),
                    unfoldingFile.getAbsolutePath(), directory, needLib);
            Result<? extends MpsatSynthesisOutput> mpsatResult = framework.getTaskManager().execute(
                    mpsatTask, "Running synthesis [MPSat]", subtaskMonitor);

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatSynthesisChainOutput(exportResult, punfResult, mpsatResult, settings));
            }
            monitor.progressUpdate(1.0);

            return new Result<>(Outcome.SUCCESS,
                    new MpsatSynthesisChainOutput(exportResult, punfResult, mpsatResult, settings));
        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    public MpsatSynthesisParameters getSettings() {
        return settings;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

}
