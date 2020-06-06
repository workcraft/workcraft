package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.Collection;

public class VerificationChainTask implements Task<VerificationChainOutput> {

    private final WorkspaceEntry we;
    private final VerificationParameters verificationParameters;
    private final Collection<Mutex> mutexes;

    public VerificationChainTask(WorkspaceEntry we, VerificationParameters verificationParameters) {
        this(we, verificationParameters, null);
    }

    public VerificationChainTask(WorkspaceEntry we, VerificationParameters verificationParameters,
            Collection<Mutex> mutexes) {

        this.we = we;
        this.verificationParameters = verificationParameters;
        this.mutexes = mutexes;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        TaskManager taskManager = Framework.getInstance().getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        try {
            PetriModel model = WorkspaceUtils.getAs(we, PetriModel.class);
            StgFormat format = StgFormat.getInstance();
            Exporter exporter = ExportUtils.chooseBestExporter(model, format);
            if (exporter == null) {
                throw new NoExporterException(model, StgFormat.getInstance());
            }
            SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);

            // Generate .g for the model
            File netFile = new File(directory, StgUtils.SPEC_FILE_PREFIX + format.getExtension());
            ExportTask exportTask = new ExportTask(exporter, model, netFile);
            Result<? extends ExportOutput> exportResult = taskManager.execute(
                    exportTask, "Exporting .g", subtaskMonitor);

            if (!exportResult.isSuccess()) {
                if (exportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, null, null, verificationParameters));
            }
            if ((mutexes != null) && !mutexes.isEmpty()) {
                Stg stg = StgUtils.loadStg(netFile);
                MutexUtils.factoroutMutexs(stg, mutexes);
                netFile = new File(directory, StgUtils.SPEC_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + format.getExtension());
                exportTask = new ExportTask(exporter, model, netFile);
                exportResult = taskManager.execute(exportTask, "Exporting .g");

                if (!exportResult.isSuccess()) {
                    if (exportResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            exportResult, null, null, null, verificationParameters));
                }
            }
            monitor.progressUpdate(0.33);

            // Generate unfolding
            File unfoldingFile = new File(directory, "unfolding" + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(netFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = taskManager.execute(punfTask, "Unfolding .g", subtaskMonitor);

            if (!punfResult.isSuccess()) {
                if (punfResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, punfResult, null, verificationParameters));
            }
            monitor.progressUpdate(0.66);

            // Run MPSat on the generated unfolding
            MpsatTask mpsatTask = new MpsatTask(unfoldingFile, netFile, verificationParameters, directory);
            Result<? extends MpsatOutput> mpsatResult = taskManager.execute(
                    mpsatTask, "Running verification [MPSat]", subtaskMonitor);

            if (!mpsatResult.isSuccess()) {
                if (mpsatResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        exportResult, null, punfResult, mpsatResult, verificationParameters));
            }
            monitor.progressUpdate(1.0);

            return Result.success(new VerificationChainOutput(
                    exportResult, null, punfResult, mpsatResult, verificationParameters));
        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    public VerificationParameters getVerificationParameters() {
        return verificationParameters;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

}
