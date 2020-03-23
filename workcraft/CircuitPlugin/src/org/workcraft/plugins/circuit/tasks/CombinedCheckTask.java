package org.workcraft.plugins.circuit.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.CombinedChainOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationTask;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
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
                return new Result<>(Result.Outcome.SUCCESS,
                        new CombinedChainOutput(null, null, null, new ArrayList<>(),
                                verificationParametersList, vacuousMessage));

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
            if (devExportResult.getOutcome() != Outcome.SUCCESS) {
                if (devExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new CombinedChainOutput(devExportResult, null, null, null, verificationParametersList));
            }
            monitor.progressUpdate(0.10);

            // Generating system .g for custom property check (only if needed)
            File sysStgFile = null;
            File detailFile = null;
            Result<? extends PcompOutput>  pcompResult = null;
            if (envStg == null) {
                sysStgFile = devStgFile;
            } else {
                File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
                Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);
                if (envExportResult.getOutcome() != Outcome.SUCCESS) {
                    if (envExportResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new CombinedChainOutput(envExportResult, null, null, null, verificationParametersList));
                }

                // Generating .g for the whole system (circuit and environment)
                sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + stgFileExtension);
                detailFile = new File(directory, StgUtils.DETAIL_FILE_PREFIX + StgUtils.XML_FILE_EXTENSION);
                pcompResult = CircuitStgUtils.composeDevWithEnv(devStgFile, envStgFile, sysStgFile, detailFile, directory, monitor);
                if (pcompResult.getOutcome() != Outcome.SUCCESS) {
                    if (pcompResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new CombinedChainOutput(devExportResult, pcompResult, null, null, verificationParametersList));
                }
            }
            monitor.progressUpdate(0.20);

            // Generate unfolding (only if needed)
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(sysStgFile, unfoldingFile, directory);
            SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends PunfOutput> punfResult = manager.execute(punfTask, "Unfolding .g", punfMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new CombinedChainOutput(devExportResult, pcompResult, punfResult, null, verificationParametersList));
            }
            monitor.progressUpdate(0.40);

            // Run MPSat on the generated unfolding
            SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
            ArrayList<Result<? extends VerificationOutput>> mpsatResultList = new ArrayList<>(verificationParametersList.size());
            for (VerificationParameters verificationParameters : verificationParametersList) {
                VerificationTask verificationTask = new VerificationTask(verificationParameters.getMpsatArguments(directory), unfoldingFile, directory, sysStgFile);
                Result<? extends VerificationOutput> mpsatResult = manager.execute(
                        verificationTask, "Running verification [MPSat]", mpsatMonitor);
                mpsatResultList.add(mpsatResult);
                if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                    if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new CombinedChainOutput(devExportResult, pcompResult, punfResult, mpsatResultList, verificationParametersList));
                }
            }
            monitor.progressUpdate(1.0);

            return new Result<>(Outcome.SUCCESS,
                    new CombinedChainOutput(devExportResult, pcompResult, punfResult, mpsatResultList, verificationParametersList));
        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
