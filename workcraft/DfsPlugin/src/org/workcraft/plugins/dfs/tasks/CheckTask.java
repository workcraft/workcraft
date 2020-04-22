package org.workcraft.plugins.dfs.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.DfsToStgConverter;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatOutput;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatOutputParser;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.*;
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
        VerificationParameters preparationParameters = ReachUtils.getToolchainPreparationParameters();
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
            ExportTask exportTask = new ExportTask(exporter, model, netFile);
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(
                    exportTask, "Exporting .g", mon);

            if (!exportResult.isSuccess()) {
                if (exportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.20);

            File unfoldingFile = new File(directory, "unfolding" + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(netFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = framework.getTaskManager().execute(
                    punfTask, "Unfolding .g", mon);

            if (!punfResult.isSuccess()) {
                if (punfResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, punfResult, null, preparationParameters));
            }
            monitor.progressUpdate(0.40);

            VerificationParameters deadlockParameters = ReachUtils.getDeadlockParameters();
            MpsatTask deadlockMpsatTask = new MpsatTask(unfoldingFile, netFile, deadlockParameters, directory);
            Result<? extends MpsatOutput> deadlockMpsatResult = framework.getTaskManager().execute(
                    deadlockMpsatTask, "Running deadlock checking [MPSat]", mon);

            if (!deadlockMpsatResult.isSuccess()) {
                if (deadlockMpsatResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, punfResult, deadlockMpsatResult, deadlockParameters));
            }
            monitor.progressUpdate(0.60);

            String deadlockMpsatStdout = deadlockMpsatResult.getPayload().getStdoutString();
            MpsatOutputParser deadlockMpsatResultParser = new MpsatOutputParser(deadlockMpsatStdout);
            if (!deadlockMpsatResultParser.getSolutions().isEmpty()) {
                return Result.success(new VerificationChainOutput(
                        exportResult, null, punfResult, deadlockMpsatResult, deadlockParameters,
                        "Dataflow has a deadlock"));
            }
            monitor.progressUpdate(0.70);

            VerificationParameters persistencyParameters = ReachUtils.getOutputPersistencyParameters();
            MpsatTask persistencyMpsatTask = new MpsatTask(unfoldingFile, netFile, persistencyParameters, directory);
            Result<? extends MpsatOutput> persistencyMpsatResult = framework.getTaskManager().execute(persistencyMpsatTask,
                    "Running semimodularity checking [MPSat]", mon);
            if (!persistencyMpsatResult.isSuccess()) {
                if (persistencyMpsatResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, punfResult, persistencyMpsatResult, persistencyParameters));
            }
            monitor.progressUpdate(0.90);

            String persistencyMpsatStdout = persistencyMpsatResult.getPayload().getStdoutString();
            MpsatOutputParser persistencyMpsatResultParser = new MpsatOutputParser(persistencyMpsatStdout);
            if (!persistencyMpsatResultParser.getSolutions().isEmpty()) {
                return Result.success(new VerificationChainOutput(
                        exportResult, null, punfResult, persistencyMpsatResult, persistencyParameters,
                        "Dataflow is not output-persistent"));
            }
            monitor.progressUpdate(1.0);

            VerificationParameters completionParameters = ReachUtils.getToolchainCompletionParameters();
            return Result.success(new VerificationChainOutput(
                    exportResult, null, punfResult, null, completionParameters,
                    "Dataflow is deadlock-free and output-persistent"));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
