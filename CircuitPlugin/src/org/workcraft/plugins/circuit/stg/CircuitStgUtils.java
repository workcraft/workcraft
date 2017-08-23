package org.workcraft.plugins.circuit.stg;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;

public class CircuitStgUtils {

    public static CircuitToStgConverter createCircuitToStgConverter(VisualCircuit circuit) {
        CircuitToStgConverter converter = new CircuitToStgConverter(circuit);
        File envWorkFile = circuit.getEnvironmentFile();
        if ((envWorkFile != null) && envWorkFile.exists()) {
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
            Set<String> inputSignalNames = devStg.getSignalNames(Type.INPUT, null);
            Set<String> outputSignalNames = devStg.getSignalNames(Type.OUTPUT, null);
            File envStgFile = exportEnvStg(envWorkFile, inputSignalNames, outputSignalNames, directory);
            if (envStgFile != null) {
                envStgFile.deleteOnExit();
            }
            // Generating .g for the whole system (circuit and environment)
            File sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + StgUtils.ASTG_FILE_EXT);
            sysStgFile.deleteOnExit();
            Result<? extends ExternalProcessResult> pcompResult = composeDevWithEnv(devStgFile, envStgFile, sysStgFile,
                    null, directory, null);

            switch (pcompResult.getOutcome()) {
            case FINISHED:
                break;
            case CANCELLED:
                sysStgFile = null;
                break;
            case FAILED:
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
            result = exportStg(envStg, StgUtils.ENVIRONMENT_FILE_NAME + StgUtils.ASTG_FILE_EXT, directory);
        }
        return result;
    }

    private static File exportDevStg(Stg devStg, File directory) throws IOException {
        return exportStg(devStg, StgUtils.DEVICE_FILE_NAME + StgUtils.ASTG_FILE_EXT, directory);
    }

    private static File exportSytemStg(Stg systemStg, File directory) throws IOException {
        return exportStg(systemStg, StgUtils.SYSTEM_FILE_NAME + StgUtils.ASTG_FILE_EXT, directory);
    }

    private static File exportStg(Stg stg, String fileName, File directory) throws IOException {
        File stgFile = new File(directory, fileName);
        Result<? extends Object> exportResult = exportStg(stg, stgFile, directory, null);

        switch (exportResult.getOutcome()) {
        case FINISHED:
            break;
        case CANCELLED:
            stgFile = null;
            break;
        case FAILED:
            throw new RuntimeException("Export failed for file '" + fileName + "':\n" + exportResult.getCause());
        }
        return stgFile;
    }

    public static Result<? extends Object> exportStg(Stg stg, File stgFile, File directory,
            ProgressMonitor<? super MpsatChainResult> monitor) {

        Framework framework = Framework.getInstance();
        PluginManager pluginManager = framework.getPluginManager();
        Exporter stgExporter = Export.chooseBestExporter(pluginManager, stg, StgFormat.getInstance());
        if (stgExporter == null) {
            throw new RuntimeException("Exporter not available: model class " + stg.getClass().getName() + " to .g format.");
        }

        ExportTask exportTask = new ExportTask(stgExporter, stg, stgFile.getAbsolutePath());
        String description = "Exporting " + stgFile.getAbsolutePath();
        SubtaskMonitor<Object> subtaskMonitor = null;
        if (monitor != null) {
            subtaskMonitor = new SubtaskMonitor<Object>(monitor);
        }
        TaskManager taskManager = framework.getTaskManager();
        return taskManager.execute(exportTask, description, subtaskMonitor);
    }

    public static Result<? extends ExternalProcessResult> composeDevWithEnv(File devStgFile, File envStgFile, File sysStgFile,
            File placesFile, File directory, ProgressMonitor<? super MpsatChainResult> monitor) {
        Framework framework = Framework.getInstance();
        File[] inputFiles = new File[]{devStgFile, envStgFile};
        PcompTask pcompTask = new PcompTask(inputFiles, sysStgFile, placesFile, ConversionMode.OUTPUT, true, false, directory);
        String description = "Running parallel composition [PComp]";
        SubtaskMonitor<Object> subtaskMonitor = null;
        if (monitor != null) {
            subtaskMonitor = new SubtaskMonitor<Object>(monitor);
        }
        TaskManager taskManager = framework.getTaskManager();
        return taskManager.execute(pcompTask, description, subtaskMonitor);
    }

}
