package org.workcraft.plugins.circuit.stg;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;

public class CircuitStgUtils {

    public static Result<? extends Object> exportStg(Stg stg, File stgFile, File directory,
            ProgressMonitor<?> monitor) {

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
            subtaskMonitor = new SubtaskMonitor<>(monitor);
        }
        TaskManager taskManager = framework.getTaskManager();
        return taskManager.execute(exportTask, description, subtaskMonitor);
    }

    public static Result<? extends ExternalProcessResult> composeDevWithEnv(File devStgFile, File envStgFile,
            File sysStgFile, File detailsFile, File directory, ProgressMonitor<?> monitor) {

        Framework framework = Framework.getInstance();
        File[] inputFiles = new File[]{devStgFile, envStgFile};
        PcompTask pcompTask = new PcompTask(inputFiles, sysStgFile, detailsFile, ConversionMode.OUTPUT, true, false, directory);
        String description = "Running parallel composition [PComp]";
        SubtaskMonitor<Object> subtaskMonitor = null;
        if (monitor != null) {
            subtaskMonitor = new SubtaskMonitor<>(monitor);
        }
        TaskManager taskManager = framework.getTaskManager();
        return taskManager.execute(pcompTask, description, subtaskMonitor);
    }

}
