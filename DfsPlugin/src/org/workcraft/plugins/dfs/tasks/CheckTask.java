package org.workcraft.plugins.dfs.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.DfsToStgConverter;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationOutputParser;
import org.workcraft.plugins.mpsat.tasks.VerificationTask;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class CheckTask implements Task<VerificationChainOutput> {

    private final WorkspaceEntry we;

    public CheckTask(WorkspaceEntry we) {
        this.we = we;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        final Framework framework = Framework.getInstance();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        VerificationParameters preparationSettings = VerificationParameters.getToolchainPreparationSettings();
        try {
            VisualDfs dfs = WorkspaceUtils.getAs(we, VisualDfs.class);
            DfsToStgConverter converter = new DfsToStgConverter(dfs);
            StgModel model = converter.getStgModel().getMathModel();
            StgFormat format = StgFormat.getInstance();
            Exporter exporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), model, format);
            if (exporter == null) {
                throw new NoExporterException(model, StgFormat.getInstance());
            }
            monitor.progressUpdate(0.10);

            File netFile = new File(directory, "net" + format.getExtension());
            ExportTask exportTask = new ExportTask(exporter, model, netFile.getAbsolutePath());
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(
                    exportTask, "Exporting .g", mon);

            if (exportResult.getOutcome() != Outcome.SUCCESS) {
                if (exportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(exportResult, null, null, null, preparationSettings));
            }
            monitor.progressUpdate(0.20);

            File unfoldingFile = new File(directory, "unfolding" + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(netFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = framework.getTaskManager().execute(
                    punfTask, "Unfolding .g", mon);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(exportResult, null, punfResult, null, preparationSettings));
            }
            monitor.progressUpdate(0.40);

            VerificationParameters deadlockSettings = VerificationParameters.getDeadlockSettings();
            VerificationTask deadlockVerificationTask = new VerificationTask(deadlockSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, netFile);
            Result<? extends VerificationOutput> deadlockMpsatResult = framework.getTaskManager().execute(
                    deadlockVerificationTask, "Running deadlock checking [MPSat]", mon);

            if (deadlockMpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (deadlockMpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(exportResult, null, punfResult, deadlockMpsatResult, deadlockSettings));
            }
            monitor.progressUpdate(0.60);

            VerificationOutputParser deadlockMpsatResultParser = new VerificationOutputParser(deadlockMpsatResult.getPayload());
            if (!deadlockMpsatResultParser.getSolutions().isEmpty()) {
                return new Result<>(Outcome.SUCCESS,
                        new VerificationChainOutput(exportResult, null, punfResult, deadlockMpsatResult, deadlockSettings,
                                "Dataflow has a deadlock"));
            }
            monitor.progressUpdate(0.70);

            VerificationParameters persistencySettings = VerificationParameters.getOutputPersistencySettings();
            VerificationTask persistencyVerificationTask = new VerificationTask(persistencySettings.getMpsatArguments(directory),
                    unfoldingFile, directory, netFile);
            Result<? extends VerificationOutput> persistencyMpsatResult = framework.getTaskManager().execute(persistencyVerificationTask,
                    "Running semimodularity checking [MPSat]", mon);
            if (persistencyMpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (persistencyMpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(exportResult, null, punfResult, persistencyMpsatResult, persistencySettings));
            }
            monitor.progressUpdate(0.90);

            VerificationOutputParser persistencyMpsatResultParser = new VerificationOutputParser(persistencyMpsatResult.getPayload());
            if (!persistencyMpsatResultParser.getSolutions().isEmpty()) {
                return new Result<>(Outcome.SUCCESS,
                        new VerificationChainOutput(exportResult, null, punfResult, persistencyMpsatResult, persistencySettings,
                                "Dataflow is not output-persistent"));
            }
            monitor.progressUpdate(1.0);

            VerificationParameters completionSettings = VerificationParameters.getToolchainCompletionSettings();
            return new Result<>(Outcome.SUCCESS,
                    new VerificationChainOutput(exportResult, null, punfResult, null, completionSettings,
                            "Dataflow is deadlock-free and output-persistent"));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
