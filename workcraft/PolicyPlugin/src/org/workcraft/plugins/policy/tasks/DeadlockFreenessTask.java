package org.workcraft.plugins.policy.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatOutput;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.policy.VisualPolicy;
import org.workcraft.plugins.policy.converters.PolicyToPetriConverter;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.*;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class DeadlockFreenessTask implements Task<VerificationChainOutput> {

    private final WorkspaceEntry we;

    public DeadlockFreenessTask(WorkspaceEntry we) {
        this.we = we;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        TaskManager taskManager = Framework.getInstance().getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        VerificationParameters verificationParameters = ReachUtils.getDeadlockParameters();
        try {
            VisualPolicy policy = WorkspaceUtils.getAs(we, VisualPolicy.class);
            PolicyToPetriConverter converter = new PolicyToPetriConverter(policy);
            Petri model = converter.getPetriNet().getMathModel();
            StgFormat format = StgFormat.getInstance();
            Exporter exporter = ExportUtils.chooseBestExporter(model, format);
            if (exporter == null) {
                throw new NoExporterException(model, format);
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
                        exportResult, null, null, verificationParameters));
            }
            monitor.progressUpdate(0.20);

            MpsatTask mpsatTask = new MpsatTask(netFile, verificationParameters, directory);
            Result<? extends MpsatOutput> mpsatResult = taskManager.execute(
                    mpsatTask, "Running deadlock checking [MPSat]", mon);

            if (!mpsatResult.isSuccess()) {
                if (mpsatResult.isCancel()) {
                    return Result.cancel();
                }
                String errorMessage = mpsatResult.getPayload().getErrorsHeadAndTail();
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, mpsatResult, verificationParameters, errorMessage));
            }
            monitor.progressUpdate(0.90);

            if (mpsatResult.getPayload().hasSolutions()) {
                return Result.success(new VerificationChainOutput(
                        exportResult, null, mpsatResult, verificationParameters, "Policy net has a deadlock"));
            }
            monitor.progressUpdate(1.0);

            return Result.success(new VerificationChainOutput(
                    exportResult, null, mpsatResult, verificationParameters, "Policy net is deadlock-free"));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
