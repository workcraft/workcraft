package org.workcraft.plugins.circuit.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatOutput;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatOutputParser;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.Set;

public class AssertionCheckTask implements Task<VerificationChainOutput> {

    private final WorkspaceEntry we;
    private final VerificationParameters verificationParameters;

    public AssertionCheckTask(WorkspaceEntry we, VerificationParameters verificationParameters) {
        this.we = we;
        this.verificationParameters = verificationParameters;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        VerificationParameters preparationParameters = ReachUtils.getToolchainPreparationParameters();
        try {
            // Common variables
            VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
            File envFile = circuit.getMathModel().getEnvironmentFile();

            // Load device STG
            CircuitToStgConverter converter = new CircuitToStgConverter(circuit);
            Stg devStg = converter.getStg().getMathModel();

            // Load environment STG
            Stg envStg = StgUtils.loadStg(envFile);
            if (envStg != null) {
                // Make sure that input signals of the device STG are also inputs in the environment STG
                Set<String> inputSignalNames = devStg.getSignalNames(Signal.Type.INPUT, null);
                Set<String> outputSignalNames = devStg.getSignalNames(Signal.Type.OUTPUT, null);
                StgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);
            }

            // Write device STG into a .g file
            String devStgName = (envStg != null ? StgUtils.DEVICE_FILE_PREFIX : StgUtils.SYSTEM_FILE_PREFIX) + stgFileExtension;
            File devStgFile = new File(directory, devStgName);
            Result<? extends ExportOutput> devExportResult = StgUtils.exportStg(devStg, devStgFile, monitor);
            if (!devExportResult.isSuccess()) {
                if (devExportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        devExportResult, null, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.10);

            // Generating system .g for custom property check (only if needed)
            File sysStgFile = null;
            Result<? extends PcompOutput>  pcompResult = null;
            if (envStg == null) {
                sysStgFile = devStgFile;
            } else {
                File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
                Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);
                if (!envExportResult.isSuccess()) {
                    if (envExportResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            envExportResult, null, null, null, preparationParameters));
                }

                // Generating .g for the whole system (circuit and environment)
                pcompResult = CircuitStgUtils.composeDevWithEnv(devStgFile, envStgFile, directory, monitor);
                if (!pcompResult.isSuccess()) {
                    if (pcompResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            devExportResult, pcompResult, null, null, preparationParameters));
                }
                sysStgFile = pcompResult.getPayload().getOutputFile();
            }
            monitor.progressUpdate(0.20);

            // Generate unfolding (only if needed)
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(sysStgFile, unfoldingFile, directory);
            SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends PunfOutput> punfResult = manager.execute(punfTask, "Unfolding .g", punfMonitor);

            if (!punfResult.isSuccess()) {
                if (punfResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        devExportResult, pcompResult, punfResult, null, preparationParameters));
            }
            monitor.progressUpdate(0.40);

            // Check custom property (if requested)
            MpsatTask mpsatTask = new MpsatTask(unfoldingFile, sysStgFile, verificationParameters, directory);
            SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends MpsatOutput> mpsatResult = manager.execute(
                    mpsatTask, "Running custom property check [MPSat]", mpsatMonitor);

            if (!mpsatResult.isSuccess()) {
                if (mpsatResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        devExportResult, pcompResult, punfResult, mpsatResult, verificationParameters));
            }
            monitor.progressUpdate(0.50);

            String mpsatStdout = mpsatResult.getPayload().getStdoutString();
            MpsatOutputParser mpsatParser = new MpsatOutputParser(mpsatStdout);
            if (!mpsatParser.getSolutions().isEmpty()) {
                return Result.success(new VerificationChainOutput(
                        devExportResult, pcompResult, punfResult, mpsatResult, verificationParameters,
                        "Property is violated after the following trace(s):"));
            }
            monitor.progressUpdate(1.00);

            // Success
            return Result.success(new VerificationChainOutput(
                    devExportResult, pcompResult, punfResult, mpsatResult, verificationParameters,
                    "Property holds"));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }


}
