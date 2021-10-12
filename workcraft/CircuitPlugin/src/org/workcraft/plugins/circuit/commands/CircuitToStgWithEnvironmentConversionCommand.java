package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.utils.PcompUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;

import java.io.File;
import java.util.Set;

public class CircuitToStgWithEnvironmentConversionCommand extends CircuitToStgConversionCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph (composed with environment)";
    }

    @Override
    public CircuitToStgConverter getCircuitToStgConverter(final VisualCircuit circuit) {
        CircuitToStgConverter converter = new CircuitToStgConverter(circuit);
        File envWorkFile = circuit.getMathModel().getEnvironmentFile();
        if (envWorkFile == null) {
            DialogUtils.showWarning("Environment STG is not specified.");
        } else if (!envWorkFile.exists()) {
            DialogUtils.showWarning("Environment STG file does not exist:\n" + envWorkFile.getAbsolutePath());
        } else {
            Stg devStg = converter.getStg().getMathModel();
            String title = circuit.getTitle();
            Stg systemStg = createSystemStg(devStg, envWorkFile, title);
            if (systemStg != null) {
                converter = new CircuitToStgConverter(circuit, new VisualStg(systemStg));
            }
        }
        return converter;
    }

    private static Stg createSystemStg(Stg devStg, File envWorkFile, String title) {
        String prefix = FileUtils.getTempPrefix(title);
        File directory = FileUtils.createTempDirectory(prefix);
        File devStgFile = exportDevStg(devStg, directory);
        devStgFile.deleteOnExit();
        // Make sure that input signals of the device STG are also inputs in the environment STG
        Set<String> inputSignalNames = devStg.getSignalNames(Signal.Type.INPUT, null);
        Set<String> outputSignalNames = devStg.getSignalNames(Signal.Type.OUTPUT, null);
        File envStgFile = exportEnvStg(envWorkFile, inputSignalNames, outputSignalNames, directory);
        if (envStgFile != null) {
            envStgFile.deleteOnExit();
        }
        // Generating .g for the whole system (circuit and environment)
        Result<? extends PcompOutput> pcompResult = PcompUtils.composeDevWithEnv(
                devStgFile, envStgFile, directory, null);

        if (pcompResult.isSuccess()) {
            File sysStgFile = pcompResult.getPayload().getOutputFile();
            return StgUtils.loadStg(sysStgFile);
        }

        if (pcompResult.isFailure()) {
            throw new RuntimeException("Composition failed:\n" + pcompResult.getCause());
        }

        return null;
    }

    private static File exportEnvStg(File envFile, Set<String> inputSignalNames, Set<String> outputSignalNames,
            File directory) {

        File result = null;
        Stg envStg = StgUtils.loadStg(envFile);
        if (envStg != null) {
            StgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);
            String stgFileExtension = StgFormat.getInstance().getExtension();
            result = exportStg(envStg, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension, directory);
        }
        return result;
    }

    private static File exportDevStg(Stg devStg, File directory) {
        String stgFileExtension = StgFormat.getInstance().getExtension();
        return exportStg(devStg, StgUtils.DEVICE_FILE_PREFIX + stgFileExtension, directory);
    }

    private static File exportStg(Stg stg, String fileName, File directory) {
        File stgFile = new File(directory, fileName);
        Result<? extends ExportOutput> exportResult = StgUtils.exportStg(stg, stgFile, null);

        if (exportResult.isFailure()) {
            throw new RuntimeException("Export failed for file '" + fileName + "':\n" + exportResult.getCause());
        }

        if (exportResult.isCancel()) {
            stgFile = null;
        }

        return stgFile;
    }

}
