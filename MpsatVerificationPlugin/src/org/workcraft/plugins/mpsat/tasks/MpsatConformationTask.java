package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatConformationTask extends MpsatChainTask {

    private final MpsatParameters toolchainPreparationSettings = new MpsatParameters("Toolchain preparation of data",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final MpsatParameters toolchainCompletionSettings = new MpsatParameters("Toolchain completion",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final File envFile;

    public MpsatConformationTask(WorkspaceEntry we, File envFile) {
        super(we, null);
        this.envFile = envFile;
    }

    @Override
    public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
        Framework framework = Framework.getInstance();
        WorkspaceEntry we = getWorkspaceEntry();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        try {
            Stg devStg = WorkspaceUtils.getAs(we, Stg.class);
            Exporter devStgExporter = Export.chooseBestExporter(framework.getPluginManager(), devStg, StgFormat.getInstance());
            if (devStgExporter == null) {
                throw new RuntimeException("Exporter not available: model class " + devStg.getClass().getName() + " to format STG.");
            }
            SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);

            // Generating .g for the model
            File devStgFile = new File(directory, StgUtils.DEVICE_FILE_PREFIX + stgFileExtension);
            ExportTask devExportTask = new ExportTask(devStgExporter, devStg, devStgFile.getAbsolutePath());
            Result<? extends Object> devExportResult = framework.getTaskManager().execute(
                    devExportTask, "Exporting circuit .g", subtaskMonitor);

            if (devExportResult.getOutcome() != Outcome.SUCCESS) {
                if (devExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(devExportResult, null, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.30);

            // Generating .g for the environment
            Stg envStg = StgUtils.loadStg(envFile);
            if (envStg == null) {
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(null, null, null, null, toolchainPreparationSettings));
            }

            // Make sure that input signals of the device STG are also inputs in the environment STG
            Set<String> inputSignalNames = devStg.getSignalNames(Type.INPUT, null);
            Set<String> outputSignalNames = devStg.getSignalNames(Type.OUTPUT, null);
            StgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);
            Exporter envStgExporter = Export.chooseBestExporter(framework.getPluginManager(), envStg, StgFormat.getInstance());
            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
            ExportTask envExportTask = new ExportTask(envStgExporter, envStg, envStgFile.getAbsolutePath());
            Result<? extends Object> envExportResult = framework.getTaskManager().execute(
                    envExportTask, "Exporting environment .g", subtaskMonitor);

            if (envExportResult.getOutcome() != Outcome.SUCCESS) {
                if (envExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(envExportResult, null, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.40);

            // Generating .g for the whole system (model and environment)
            File placesFile = new File(directory, StgUtils.COMP_FILE_PREFIX + StgUtils.COMP_FILE_EXTENSION);
            File stgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + stgFileExtension);
            stgFile.deleteOnExit();
            PcompTask pcompTask = new PcompTask(new File[]{devStgFile, envStgFile}, stgFile, placesFile,
                    ConversionMode.OUTPUT, true, false, directory);

            Result<? extends ExternalProcessResult> pcompResult = framework.getTaskManager().execute(
                    pcompTask, "Running parallel composition [PComp]", subtaskMonitor);

            if (pcompResult.getOutcome() != Outcome.SUCCESS) {
                if (pcompResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(devExportResult, pcompResult, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.50);

            // Generate unfolding
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(stgFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(
                    punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(devExportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.60);

            // Check for interface conformation
            CompositionData compositionData = new CompositionData(placesFile);
            ComponentData devComponentData = compositionData.getComponentData(devStgFile);
            Set<String> devPlaceNames = devComponentData.getDstPlaces();
            MpsatParameters conformationSettings = MpsatParameters.getConformationSettings(devPlaceNames);
            MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, stgFile, placesFile);
            Result<? extends ExternalProcessResult>  mpsatConformationResult = framework.getTaskManager().execute(
                    mpsatConformationTask, "Running conformation check [MPSat]", subtaskMonitor);

            if (mpsatConformationResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatConformationResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings));
            }
            monitor.progressUpdate(0.80);

            MpsatResultParser mpsatConformationParser = new MpsatResultParser(mpsatConformationResult.getReturnValue());
            if (!mpsatConformationParser.getSolutions().isEmpty()) {
                return new Result<MpsatChainResult>(Outcome.SUCCESS,
                        new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings,
                                "This model does not conform to the environment."));
            }
            monitor.progressUpdate(1.0);

            // Success
            unfoldingFile.delete();
            String message = "The model conforms to its environment (" + envFile.getName() + ").";
            return new Result<MpsatChainResult>(Outcome.SUCCESS,
                    new MpsatChainResult(devExportResult, pcompResult, punfResult, null, toolchainCompletionSettings, message));

        } catch (Throwable e) {
            return new Result<MpsatChainResult>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
