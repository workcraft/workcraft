package org.workcraft.plugins.circuit.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.CombinedChainOutput;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatOutput;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatTask;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatUnfoldingTask;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.utils.PcompUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CombinedCheckTask implements Task<CombinedChainOutput> {

    private final WorkspaceEntry we;
    private final List<VerificationParameters> verificationParametersList;
    private final String vacuousMessage;

    public CombinedCheckTask(WorkspaceEntry we, List<VerificationParameters> verificationParametersList, String vacuousMessage) {
        this.we = we;
        this.verificationParametersList = verificationParametersList;
        this.vacuousMessage = vacuousMessage;
    }

    @Override
    public Result<? extends CombinedChainOutput> run(ProgressMonitor<? super CombinedChainOutput> monitor) {
        TaskManager manager = Framework.getInstance().getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        try {
            if (verificationParametersList.isEmpty()) {
                return Result.success(new CombinedChainOutput(null, null, null,
                        new ArrayList<>(), verificationParametersList, vacuousMessage));

            }
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
                return Result.failure(new CombinedChainOutput(
                        devExportResult, null, null, null, verificationParametersList));
            }
            monitor.progressUpdate(0.10);

            // Generating system .g for custom property check (only if needed)
            File sysStgFile;
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
                    return Result.failure(new CombinedChainOutput(
                            envExportResult, null, null, null, verificationParametersList));
                }

                // Generating .g for the whole system (circuit and environment)
                pcompResult = PcompUtils.composeDevWithEnv(devStgFile, envStgFile, directory, monitor);
                if (!pcompResult.isSuccess()) {
                    if (pcompResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new CombinedChainOutput(
                            devExportResult, pcompResult, null, null, verificationParametersList));
                }
                sysStgFile = pcompResult.getPayload().getOutputFile();
            }
            monitor.progressUpdate(0.20);

            // Generate unfolding (only if needed)
            File unfoldingFile = new File(directory, MpsatUnfoldingTask.UNFOLDING_FILE_NAME);
            MpsatUnfoldingTask unfoldingTask = new MpsatUnfoldingTask(sysStgFile, unfoldingFile, directory);
            SubtaskMonitor<Object> unfoldingMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends MpsatOutput> unfoldingResult = manager.execute(unfoldingTask, "Unfolding .g", unfoldingMonitor);

            if (!unfoldingResult.isSuccess()) {
                if (unfoldingResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new CombinedChainOutput(
                        devExportResult, pcompResult, unfoldingResult, null, verificationParametersList));
            }
            monitor.progressUpdate(0.40);

            // Run MPSat on the generated unfolding
            SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
            ArrayList<Result<? extends MpsatOutput>> mpsatResultList = new ArrayList<>(verificationParametersList.size());
            for (VerificationParameters verificationParameters : verificationParametersList) {
                MpsatTask mpsatTask = new MpsatTask(unfoldingFile, sysStgFile, verificationParameters, directory);
                Result<? extends MpsatOutput> mpsatResult = manager.execute(
                        mpsatTask, "Running verification [MPSat]", mpsatMonitor);

                mpsatResultList.add(mpsatResult);
                if (!mpsatResult.isSuccess()) {
                    if (mpsatResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new CombinedChainOutput(
                            devExportResult, pcompResult, unfoldingResult, mpsatResultList, verificationParametersList));
                }
            }
            monitor.progressUpdate(1.0);

            return Result.success(new CombinedChainOutput(
                    devExportResult, pcompResult, unfoldingResult, mpsatResultList, verificationParametersList));
        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
