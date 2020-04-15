package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CombinedChainTask implements Task<CombinedChainOutput> {

    private final WorkspaceEntry we;
    private final List<VerificationParameters> verificationParametersList;
    private final Task<VerificationChainOutput> extraTask;

    public CombinedChainTask(WorkspaceEntry we, List<VerificationParameters> verificationParametersList,
            Task<VerificationChainOutput> extraTask) {

        this.we = we;
        this.verificationParametersList = verificationParametersList;
        this.extraTask = extraTask;
    }

    @Override
    public Result<? extends CombinedChainOutput> run(ProgressMonitor<? super CombinedChainOutput> monitor) {
        Result<? extends CombinedChainOutput> chainResult = processSettingList(monitor);

        // Only proceed with the extra task if the main tasks are all successful and have no solutions.
        if ((extraTask != null) && (chainResult.getOutcome() == Outcome.SUCCESS)
                && (CombinedChainResultHandlingMonitor.getViolationMpsatOutput(chainResult) == null)) {

            Result<? extends VerificationChainOutput> taskResult = processExtraTask(monitor);
            if (taskResult.getOutcome() == Outcome.CANCEL) {
                return new Result<>(Outcome.CANCEL);
            }

            VerificationChainOutput payload = taskResult.getPayload();

            Result<? extends ExportOutput> exportResult = payload.getExportResult();
            Result<? extends PcompOutput> pcompResult = payload.getPcompResult();
            Result<? extends PunfOutput> punfResult = payload.getPunfResult();
            List<Result<? extends MpsatOutput>> mpsatResultList = chainResult.getPayload().getMpsatResultList();
            mpsatResultList.add(payload.getMpsatResult());
            verificationParametersList.add(payload.getVerificationParameters());

            chainResult = new Result<>(taskResult.getOutcome(),
                    new CombinedChainOutput(exportResult, pcompResult, punfResult, mpsatResultList, verificationParametersList));
        }
        return chainResult;
    }

    private Result<? extends CombinedChainOutput> processSettingList(ProgressMonitor<? super CombinedChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        PluginManager pluginManager = framework.getPluginManager();
        TaskManager taskManager = framework.getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        ArrayList<Result<? extends MpsatOutput>> mpsatResultList = new ArrayList<>(verificationParametersList.size());
        try {
            PetriModel model = WorkspaceUtils.getAs(we, PetriModel.class);
            StgFormat format = StgFormat.getInstance();
            Exporter exporter = ExportUtils.chooseBestExporter(pluginManager, model, format);
            if (exporter == null) {
                throw new NoExporterException(model, format);
            }
            SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);

            // Generate .g for the model
            File netFile = new File(directory, "net" + format.getExtension());
            ExportTask exportTask = new ExportTask(exporter, model, netFile.getAbsolutePath());
            Result<? extends ExportOutput> exportResult = taskManager.execute(
                    exportTask, "Exporting .g", subtaskMonitor);

            if (exportResult.getOutcome() != Outcome.SUCCESS) {
                if (exportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new CombinedChainOutput(exportResult, null, null, mpsatResultList, verificationParametersList));
            }
            monitor.progressUpdate(0.33);

            // Generate unfolding
            File unfoldingFile = new File(directory, "unfolding" + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(netFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = taskManager.execute(punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new CombinedChainOutput(exportResult, null, punfResult, mpsatResultList, verificationParametersList));
            }
            monitor.progressUpdate(0.66);

            // Run MPSat on the generated unfolding
            for (VerificationParameters verificationParameters: verificationParametersList) {
                MpsatTask mpsatTask = new MpsatTask(unfoldingFile, netFile, verificationParameters, directory);
                Result<? extends MpsatOutput> mpsatResult = taskManager.execute(
                        mpsatTask, "Running verification [MPSat]", subtaskMonitor);
                mpsatResultList.add(mpsatResult);
                if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                    if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new CombinedChainOutput(exportResult, null, punfResult, mpsatResultList, verificationParametersList));
                }
            }
            monitor.progressUpdate(1.0);

            return new Result<>(Outcome.SUCCESS,
                    new CombinedChainOutput(exportResult, null, punfResult, mpsatResultList, verificationParametersList));
        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    private Result<? extends VerificationChainOutput> processExtraTask(ProgressMonitor<? super CombinedChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);
        return taskManager.execute(extraTask, description, subtaskMonitor);
    }

}
