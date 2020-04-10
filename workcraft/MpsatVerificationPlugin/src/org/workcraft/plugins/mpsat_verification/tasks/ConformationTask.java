package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

public class ConformationTask implements Task<VerificationChainOutput> {

    private final WorkspaceEntry we;
    private final File envFile;

    public ConformationTask(WorkspaceEntry we, File envFile) {
        this.we = we;
        this.envFile = envFile;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        PluginManager pluginManager = framework.getPluginManager();
        TaskManager taskManager = framework.getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        StgFormat format = StgFormat.getInstance();
        String stgFileExtension = format.getExtension();
        VerificationParameters preparationParameters = ReachUtils.getToolchainPreparationParameters();
        try {
            Stg devStg = WorkspaceUtils.getAs(we, Stg.class);
            Exporter devStgExporter = ExportUtils.chooseBestExporter(pluginManager, devStg, format);
            if (devStgExporter == null) {
                return Result.exception(new NoExporterException(devStg, format));
            }
            SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);

            // Generating .g for the model
            File devStgFile = new File(directory, StgUtils.DEVICE_FILE_PREFIX + stgFileExtension);
            ExportTask devExportTask = new ExportTask(devStgExporter, devStg, devStgFile.getAbsolutePath());
            Result<? extends ExportOutput> devExportResult = taskManager.execute(
                    devExportTask, "Exporting circuit .g", subtaskMonitor);

            if (devExportResult.getOutcome() != Outcome.SUCCESS) {
                if (devExportResult.getOutcome() == Outcome.CANCEL) {
                    return Result.cancelation();
                }
                return Result.failure(new VerificationChainOutput(
                        devExportResult, null, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.30);

            // Generating .g for the environment
            Stg envStg = StgUtils.loadStg(envFile);
            if (envStg == null) {
                return Result.failure(new VerificationChainOutput(
                        null, null, null, null, preparationParameters));
            }

            // Make sure that input signals of the device STG are also inputs in the environment STG
            Set<String> inputSignals = devStg.getSignalReferences(Signal.Type.INPUT);
            Set<String> outputSignals = devStg.getSignalReferences(Signal.Type.OUTPUT);
            StgUtils.restoreInterfaceSignals(envStg, inputSignals, outputSignals);
            Exporter envStgExporter = ExportUtils.chooseBestExporter(pluginManager, envStg, format);
            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
            ExportTask envExportTask = new ExportTask(envStgExporter, envStg, envStgFile.getAbsolutePath());
            Result<? extends ExportOutput> envExportResult = taskManager.execute(
                    envExportTask, "Exporting environment .g", subtaskMonitor);

            if (envExportResult.getOutcome() != Outcome.SUCCESS) {
                if (envExportResult.getOutcome() == Outcome.CANCEL) {
                    return Result.cancelation();
                }
                return Result.failure(new VerificationChainOutput(
                        envExportResult, null, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.40);

            // Generating .g for the whole system (model and environment)
            PcompParameters pcompParameters = new PcompParameters(PcompParameters.SharedSignalMode.OUTPUT, true, false);
            PcompTask pcompTask = new PcompTask(Arrays.asList(devStgFile, envStgFile), pcompParameters, directory);

            Result<? extends PcompOutput> pcompResult = taskManager.execute(
                    pcompTask, "Running parallel composition [PComp]", subtaskMonitor);

            if (pcompResult.getOutcome() != Outcome.SUCCESS) {
                if (pcompResult.getOutcome() == Outcome.CANCEL) {
                    return Result.cancelation();
                }
                return Result.failure(new VerificationChainOutput(
                        devExportResult, pcompResult, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.50);

            // Generate unfolding
            File sysStgFile = pcompResult.getPayload().getOutputFile();
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(sysStgFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = taskManager.execute(
                    punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return Result.cancelation();
                }
                return Result.failure(new VerificationChainOutput(
                        devExportResult, pcompResult, punfResult, null, preparationParameters));
            }
            monitor.progressUpdate(0.60);

            // Check for conformation
            File detailFile = pcompResult.getPayload().getDetailFile();
            CompositionData compositionData = new CompositionData(detailFile);
            ComponentData devComponentData = compositionData.getComponentData(devStgFile);
            Set<String> devPlaceNames = devComponentData.getDstPlaces();
            VerificationParameters verificationParameters = ReachUtils.getConformationParameters(devPlaceNames);
            MpsatTask mpsatTask = new MpsatTask(unfoldingFile, sysStgFile, verificationParameters, directory);
            Result<? extends MpsatOutput>  mpsatResult = taskManager.execute(
                    mpsatTask, "Running conformation check [MPSat]", subtaskMonitor);

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return Result.cancelation();
                }
                return Result.failure(new VerificationChainOutput(
                        devExportResult, pcompResult, punfResult, mpsatResult, verificationParameters));
            }
            monitor.progressUpdate(0.80);

            String mpsatStdout = mpsatResult.getPayload().getStdoutString();
            MpsatOutputParser mpsatOutputParser = new MpsatOutputParser(mpsatStdout);
            if (!mpsatOutputParser.getSolutions().isEmpty()) {
                return Result.success(new VerificationChainOutput(
                        devExportResult, pcompResult, punfResult, mpsatResult, verificationParameters,
                        "This model does not conform to the environment."));
            }
            monitor.progressUpdate(1.0);

            // Success
            return Result.success(new VerificationChainOutput(
                    devExportResult, pcompResult, punfResult, mpsatResult, verificationParameters,
                    "The model conforms to its environment (" + envFile.getName() + ")."));

        } catch (Throwable e) {
            return Result.exception(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
