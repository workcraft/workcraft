package org.workcraft.plugins.dfs.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.DfsToStgConverter;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatOutput;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatTask;
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

public class OutputPersistencyCheckTask implements Task<VerificationChainOutput> {

    private final WorkspaceEntry we;

    public OutputPersistencyCheckTask(WorkspaceEntry we) {
        this.we = we;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        TaskManager taskManager = Framework.getInstance().getTaskManager();
        VerificationParameters verificationParameters = ReachUtils.getOutputPersistencyParameters();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        StgFormat format = StgFormat.getInstance();
        String stgFileExtension = format.getExtension();
        try {
            VisualDfs dfs = WorkspaceUtils.getAs(we, VisualDfs.class);
            DfsToStgConverter converter = new DfsToStgConverter(dfs);
            StgModel model = converter.getStgModel().getMathModel();
            Exporter exporter = ExportUtils.chooseBestExporter(model, format);
            if (exporter == null) {
                throw new NoExporterException(model, format);
            }
            monitor.progressUpdate(0.10);

            File netFile = new File(directory, "net" + stgFileExtension);
            ExportTask exportTask = new ExportTask(exporter, model, netFile);
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExportOutput> exportResult = taskManager.execute(
                    exportTask, "Exporting .g", mon);

            if (!exportResult.isSuccess()) {
                if (exportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, null, verificationParameters));
            }
            monitor.progressUpdate(0.20);

            MpsatTask mpsatTask = new MpsatTask(netFile, verificationParameters, directory);
            Result<? extends MpsatOutput> mpsatResult = taskManager.execute(
                    mpsatTask, "Running output persistency check [MPSat]", mon);

            if (!mpsatResult.isSuccess()) {
                if (mpsatResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, mpsatResult, verificationParameters));
            }
            monitor.progressUpdate(0.90);

            if (mpsatResult.getPayload().hasSolutions()) {
                return Result.success(new VerificationChainOutput(
                        exportResult, null, mpsatResult, verificationParameters,
                        "Dataflow is not output-persistent"));
            }
            monitor.progressUpdate(1.0);

            return Result.success(new VerificationChainOutput(
                    exportResult, null, mpsatResult, verificationParameters,
                    "Dataflow is output-persistent"));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
