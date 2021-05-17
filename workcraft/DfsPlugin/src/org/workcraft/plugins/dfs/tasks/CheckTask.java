package org.workcraft.plugins.dfs.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.DfsToStgConverter;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatOutput;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatTask;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatUnfoldingTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
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
        TaskManager taskManager = Framework.getInstance().getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        VerificationParameters preparationParameters = ReachUtils.getToolchainPreparationParameters();
        try {
            VisualDfs dfs = WorkspaceUtils.getAs(we, VisualDfs.class);
            DfsToStgConverter converter = new DfsToStgConverter(dfs);
            StgModel model = converter.getStgModel().getMathModel();
            StgFormat format = StgFormat.getInstance();
            Exporter exporter = ExportUtils.chooseBestExporter(model, format);
            if (exporter == null) {
                throw new NoExporterException(model, StgFormat.getInstance());
            }
            monitor.progressUpdate(0.10);

            File netFile = new File(directory, "net" + format.getExtension());
            ExportTask exportTask = new ExportTask(exporter, model, netFile);
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExportOutput> exportResult = taskManager.execute(
                    exportTask, "Exporting .g", mon);

            if (!exportResult.isSuccess()) {
                if (exportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, null,  preparationParameters));
            }
            monitor.progressUpdate(0.20);

            File unfoldingFile = new File(directory, MpsatUnfoldingTask.UNFOLDING_FILE_NAME);
            MpsatUnfoldingTask unfoldingTask = new MpsatUnfoldingTask(netFile, unfoldingFile, directory);
            Result<? extends MpsatOutput> unfoldingResult = taskManager.execute(
                    unfoldingTask, "Unfolding .g", mon);

            if (!unfoldingResult.isSuccess()) {
                if (unfoldingResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, unfoldingResult, preparationParameters));
            }
            monitor.progressUpdate(0.40);

            VerificationParameters deadlockParameters = ReachUtils.getDeadlockParameters();
            MpsatTask deadlockTask = new MpsatTask(unfoldingFile, netFile, deadlockParameters, directory);
            Result<? extends MpsatOutput> deadlockResult = taskManager.execute(
                    deadlockTask, "Running deadlock checking [MPSat]", mon);

            if (!deadlockResult.isSuccess()) {
                if (deadlockResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, deadlockResult, deadlockParameters));
            }
            monitor.progressUpdate(0.60);

            if (deadlockResult.getPayload().hasSolutions()) {
                return Result.success(new VerificationChainOutput(
                        exportResult, null, deadlockResult, deadlockParameters,
                        "Dataflow has a deadlock"));
            }
            monitor.progressUpdate(0.70);

            VerificationParameters persistencyParameters = ReachUtils.getOutputPersistencyParameters();
            MpsatTask persistencyTask = new MpsatTask(unfoldingFile, netFile, persistencyParameters, directory);
            Result<? extends MpsatOutput> persistencyResult = taskManager.execute(persistencyTask,
                    "Running output persistency checking [MPSat]", mon);
            if (!persistencyResult.isSuccess()) {
                if (persistencyResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, persistencyResult, persistencyParameters));
            }
            monitor.progressUpdate(0.90);

            if (persistencyResult.getPayload().hasSolutions()) {
                return Result.success(new VerificationChainOutput(
                        exportResult, null, persistencyResult, persistencyParameters,
                        "Dataflow is not output-persistent"));
            }
            monitor.progressUpdate(1.0);

            VerificationParameters completionParameters = ReachUtils.getToolchainCompletionParameters();
            return Result.success(new VerificationChainOutput(
                    exportResult, null, null, completionParameters,
                    "Dataflow is deadlock-free and output-persistent"));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
