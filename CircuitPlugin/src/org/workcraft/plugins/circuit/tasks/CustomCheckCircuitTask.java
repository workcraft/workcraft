package org.workcraft.plugins.circuit.tasks;

import java.io.File;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.punf.PunfUtilitySettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CustomCheckCircuitTask extends MpsatChainTask {

    private final MpsatSettings toolchainPreparationSettings = new MpsatSettings("Toolchain preparation of data",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final MpsatSettings toolchainCompletionSettings = new MpsatSettings("Toolchain completion",
            MpsatMode.UNDEFINED, 0, null, 0);

    public CustomCheckCircuitTask(WorkspaceEntry we, MpsatSettings settings) {
        super(we, settings);
    }

    @Override
    public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
        Framework framework = Framework.getInstance();
        WorkspaceEntry we = getWorkspaceEntry();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        try {
            // Common variables
            VisualCircuit visualCircuit = (VisualCircuit) we.getModelEntry().getVisualModel();
            File envFile = visualCircuit.getEnvironmentFile();
            boolean hasEnvironment = (envFile != null) && envFile.exists();

            CircuitToStgConverter generator = new CircuitToStgConverter(visualCircuit);
            Stg devStg = (Stg) generator.getStg().getMathModel();
            String devStgName = (hasEnvironment ? StgUtils.DEVICE_FILE_NAME : StgUtils.SYSTEM_FILE_NAME) + StgUtils.ASTG_FILE_EXT;
            File devStgFile = new File(directory, devStgName);
            Result<? extends Object> devExportResult = CircuitStgUtils.exportStg(devStg, devStgFile, directory, monitor);
            if (devExportResult.getOutcome() != Outcome.FINISHED) {
                if (devExportResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(devExportResult, null, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.10);

            // Environment STG
            Stg envStg = null;
            if (hasEnvironment) {
                envStg = (Stg) framework.load(envFile).getMathModel();
                // Make sure that input signals of the device STG are also inputs in the environment STG
                Set<String> inputSignalNames = devStg.getSignalNames(Type.INPUT, null);
                Set<String> outputSignalNames = devStg.getSignalNames(Type.OUTPUT, null);
                CircuitStgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);
            }

            // Generating system .g for custom property check (only if needed)
            File sysStgFile = null;
            File placesFile = null;
            Result<? extends ExternalProcessResult>  pcompResult = null;
            if (envStg == null) {
                sysStgFile = devStgFile;
            } else {
                File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_NAME + StgUtils.ASTG_FILE_EXT);
                Result<? extends Object> envExportResult = CircuitStgUtils.exportStg(envStg, envStgFile, directory, monitor);
                if (envExportResult.getOutcome() != Outcome.FINISHED) {
                    if (envExportResult.getOutcome() == Outcome.CANCELLED) {
                        return new Result<MpsatChainResult>(Outcome.CANCELLED);
                    }
                    return new Result<MpsatChainResult>(Outcome.FAILED,
                            new MpsatChainResult(envExportResult, null, null, null, toolchainPreparationSettings));
                }

                // Generating .g for the whole system (circuit and environment)
                sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + StgUtils.ASTG_FILE_EXT);
                placesFile = new File(directory, StgUtils.PLACES_FILE_NAME + StgUtils.LIST_FILE_EXT);
                pcompResult = CircuitStgUtils.composeDevWithEnv(devStgFile, envStgFile, sysStgFile, placesFile, directory, monitor);
                if (pcompResult.getOutcome() != Outcome.FINISHED) {
                    if (pcompResult.getOutcome() == Outcome.CANCELLED) {
                        return new Result<MpsatChainResult>(Outcome.CANCELLED);
                    }
                    return new Result<MpsatChainResult>(Outcome.FAILED,
                            new MpsatChainResult(devExportResult, pcompResult, null, null, toolchainPreparationSettings));
                }
            }
            monitor.progressUpdate(0.20);

            // Generate unfolding (only if needed)
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + PunfUtilitySettings.getUnfoldingExtension(true));
            PunfTask punfTask = new PunfTask(sysStgFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(punfTask, "Unfolding .g", punfMonitor);

            if (punfResult.getOutcome() != Outcome.FINISHED) {
                if (punfResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(devExportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.40);

            // Check custom property (if requested)
            MpsatSettings settings = getSettings();
            MpsatTask mpsatTask = new MpsatTask(settings.getMpsatArguments(directory), unfoldingFile, directory);
            SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends ExternalProcessResult> mpsatResult = framework.getTaskManager().execute(
                    mpsatTask, "Running custom property check [MPSat]", mpsatMonitor);

            if (mpsatResult.getOutcome() != Outcome.FINISHED) {
                if (mpsatResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatResult, settings));
            }
            monitor.progressUpdate(0.50);

            MpsatResultParser mpsatParser = new MpsatResultParser(mpsatResult.getReturnValue());
            if (!mpsatParser.getSolutions().isEmpty()) {
                return new Result<MpsatChainResult>(Outcome.FINISHED,
                        new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatResult, settings,
                                "Custom property is violated after the following trace(s):"));
            }
            monitor.progressUpdate(1.00);

            // Success
            return new Result<MpsatChainResult>(Outcome.FINISHED,
                    new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatResult, toolchainCompletionSettings,
                            "Custom property holds"));

        } catch (Throwable e) {
            return new Result<MpsatChainResult>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
