package org.workcraft.plugins.circuit.commands;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.Result;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.FileUtils;

public class CircuitToStgWithEnvironmentConversionCommand extends CircuitToStgConversionCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph (composed with environment)";
    }

    @Override
    public CircuitToStgConverter getCircuitToStgConverter(final VisualCircuit circuit) {
        CircuitToStgConverter converter = new CircuitToStgConverter(circuit);
        File envWorkFile = circuit.getEnvironmentFile();
        if (envWorkFile == null) {
            DialogUtils.showWarning("Envioronment STG is not specified.");
        } else if (!envWorkFile.exists()) {
            DialogUtils.showWarning("Envioronment STG file does not exist:\n" + envWorkFile.getAbsolutePath());
        } else {
            Stg devStg = (Stg) converter.getStg().getMathModel();
            String title = circuit.getTitle();
            Stg systemStg = createSystemStg(devStg, envWorkFile, title);
            if (systemStg != null) {
                converter = new CircuitToStgConverter(circuit, new VisualStg(systemStg));
            }
        }
        return converter;
    }

    private static Stg createSystemStg(Stg devStg, File envWorkFile, String title) {
        Stg systemStg = null;
        String prefix = FileUtils.getTempPrefix(title);
        File directory = FileUtils.createTempDirectory(prefix);
        try {
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
            String stgFileExtension = StgFormat.getInstance().getExtension();
            File sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + stgFileExtension);
            sysStgFile.deleteOnExit();
            Result<? extends PcompOutput> pcompResult = CircuitStgUtils.composeDevWithEnv(
                    devStgFile, envStgFile, sysStgFile, null, directory, null);

            switch (pcompResult.getOutcome()) {
            case SUCCESS:
                break;
            case CANCEL:
                sysStgFile = null;
                break;
            case FAILURE:
                throw new RuntimeException("Composition failed:\n" + pcompResult.getCause());
            }
            systemStg = StgUtils.loadStg(sysStgFile);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
        }
        return systemStg;
    }

    private static File exportEnvStg(File envFile, Set<String> inputSignalNames, Set<String> outputSignalNames,
            File directory) throws DeserialisationException, IOException {

        File result = null;
        Stg envStg = StgUtils.loadStg(envFile);
        if (envStg != null) {
            StgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);
            String stgFileExtension = StgFormat.getInstance().getExtension();
            result = exportStg(envStg, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension, directory);
        }
        return result;
    }

    private static File exportDevStg(Stg devStg, File directory) throws IOException {
        String stgFileExtension = StgFormat.getInstance().getExtension();
        return exportStg(devStg, StgUtils.DEVICE_FILE_PREFIX + stgFileExtension, directory);
    }

    private static File exportStg(Stg stg, String fileName, File directory) throws IOException {
        File stgFile = new File(directory, fileName);
        Result<? extends ExportOutput> exportResult = CircuitStgUtils.exportStg(stg, stgFile, directory, null);

        switch (exportResult.getOutcome()) {
        case SUCCESS:
            break;
        case CANCEL:
            stgFile = null;
            break;
        case FAILURE:
            throw new RuntimeException("Export failed for file '" + fileName + "':\n" + exportResult.getCause());
        }
        return stgFile;
    }

}
