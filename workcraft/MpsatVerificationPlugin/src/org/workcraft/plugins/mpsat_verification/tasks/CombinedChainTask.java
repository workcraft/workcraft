package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.mpsat_verification.VerificationMode;
import org.workcraft.plugins.mpsat_verification.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.punf.PunfSettings;
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
        Result<? extends CombinedChainOutput> result = processSettingList(monitor);

        // Note that getCombinedChainOutcome returns Boolean, therefore can be null.
        if ((extraTask != null) && (MpsatUtils.getCombinedChainOutcome(result) == Boolean.TRUE)) {
            // Only proceed with the extra task if the main tasks are all successful and have no solutions.
            Result<? extends VerificationChainOutput> taskResult = processExtraTask(monitor);
            if (taskResult.getOutcome() == Outcome.CANCEL) {
                return new Result<>(Outcome.CANCEL);
            }

            VerificationChainOutput payload = taskResult.getPayload();

            Result<? extends ExportOutput> exportResult = payload.getExportResult();
            Result<? extends PcompOutput> pcompResult = payload.getPcompResult();
            Result<? extends PunfOutput> punfResult = payload.getPunfResult();
            List<Result<? extends MpsatOutput>> mpsatResultList = result.getPayload().getMpsatResultList();
            mpsatResultList.add(payload.getMpsatResult());
            verificationParametersList.add(payload.getVerificationParameters());

            result = new Result<>(taskResult.getOutcome(),
                    new CombinedChainOutput(exportResult, pcompResult, punfResult, mpsatResultList, verificationParametersList));
        }
        return result;
    }

    private Result<? extends CombinedChainOutput> processSettingList(ProgressMonitor<? super CombinedChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        PluginManager pluginManager = framework.getPluginManager();
        TaskManager taskManager = framework.getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
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
                        new CombinedChainOutput(exportResult, null, null, null, verificationParametersList));
            }
            monitor.progressUpdate(0.33);

            // Generate unfolding
            boolean useMci = false;
            if (PunfSettings.getUseMciCsc()) {
                useMci = true;
                for (VerificationParameters verificationParameters: verificationParametersList) {
                    useMci &= verificationParameters.getMode() == VerificationMode.RESOLVE_ENCODING_CONFLICTS;
                }
            }
            String unfoldingExtension = useMci ? PunfTask.MCI_FILE_EXTENSION : PunfTask.PNML_FILE_EXTENSION;

            File unfoldingFile = new File(directory, "unfolding" + unfoldingExtension);
            PunfTask punfTask = new PunfTask(netFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = taskManager.execute(punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new CombinedChainOutput(exportResult, null, punfResult, null, verificationParametersList));
            }
            monitor.progressUpdate(0.66);

            // Run MPSat on the generated unfolding
            ArrayList<Result<? extends MpsatOutput>> mpsatResultList = new ArrayList<>(verificationParametersList.size());
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
