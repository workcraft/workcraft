package org.workcraft.plugins.circuit.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat_verification.tasks.*;
import org.workcraft.plugins.mpsat_verification.utils.SpotUtils;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.utils.PcompUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.serialisation.SerialiserUtils;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

public class SpotChainTask implements Task<SpotChainOutput> {

    private final WorkspaceEntry we;
    private final String data;

    public SpotChainTask(WorkspaceEntry we, String data) {
        this.we = we;
        this.data = data;
    }

    @Override
    public Result<? extends SpotChainOutput> run(ProgressMonitor<? super SpotChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();

        try {
            // Convert SPOT assertion to Buechi automaton
            File spotFile = new File(directory, "assertion.spot");
            spotFile.deleteOnExit();
            try {
                FileUtils.dumpString(spotFile, TextUtils.removeLinebreaks(data));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Ltl2tgbaTask ltl2tgbaTask = new Ltl2tgbaTask(spotFile, directory);
            SubtaskMonitor<Object> ltl2tgbaMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends Ltl2tgbaOutput> ltl2tgbaResult = manager.execute(
                    ltl2tgbaTask, "Converting SPOT assertion to B\u00FCchi automaton", ltl2tgbaMonitor);

            if (!ltl2tgbaResult.isSuccess()) {
                if (ltl2tgbaResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new SpotChainOutput(ltl2tgbaResult, null, null, null));
            }
            // Failure if assertion is stutter-sensitive
            if (SpotUtils.extractStutterExample(ltl2tgbaResult.getPayload()) != null) {
                return Result.failure(new SpotChainOutput(ltl2tgbaResult));
            }
            monitor.progressUpdate(0.1);

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
                return Result.failure(new SpotChainOutput(ltl2tgbaResult, devExportResult, null, null));
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
                    return Result.failure(new SpotChainOutput(ltl2tgbaResult, envExportResult, null, null));
                }

                // Generating .g for the whole system (circuit and environment)
                pcompResult = PcompUtils.composeDevWithEnv(devStgFile, envStgFile, directory, monitor);
                if (!pcompResult.isSuccess()) {
                    if (pcompResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new SpotChainOutput(ltl2tgbaResult, envExportResult, pcompResult, null));
                }
                sysStgFile = pcompResult.getPayload().getOutputFile();
            }
            monitor.progressUpdate(0.20);

            // Add initial states to the system STG
            Stg sysModStg = StgUtils.loadStg(sysStgFile);
            File sysModStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + stgFileExtension);
            FileOutputStream sysModStgStream = new FileOutputStream(sysModStgFile);
            SerialiserUtils.writeModel(sysModStg, sysModStgStream, SerialiserUtils.Style.STG, true);
            sysModStgStream.close();
            monitor.progressUpdate(0.30);

            // Generate unfolding
            File hoaFile = ltl2tgbaResult.getPayload().getHoaFile();
            MpsatLtlxTask mpsatTask = new MpsatLtlxTask(sysModStgFile, hoaFile, directory);
            SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends MpsatOutput> mpsatResult = manager.execute(mpsatTask, "Unfolding .g", mpsatMonitor);

            if (!mpsatResult.isSuccess()) {
                if (mpsatResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new SpotChainOutput(ltl2tgbaResult, devExportResult, pcompResult, mpsatResult));
            }
            monitor.progressUpdate(1.0);

            return Result.success(new SpotChainOutput(ltl2tgbaResult, devExportResult, pcompResult, mpsatResult));
        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
