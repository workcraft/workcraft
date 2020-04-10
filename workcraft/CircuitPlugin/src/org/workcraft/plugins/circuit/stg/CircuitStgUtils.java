package org.workcraft.plugins.circuit.stg;

import org.workcraft.Framework;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.TaskManager;

import java.io.File;
import java.util.Arrays;

public class CircuitStgUtils {

    public static Result<? extends PcompOutput> composeDevWithEnv(File devStgFile, File envStgFile,
            File sysStgFile, File detailFile, File directory, ProgressMonitor<?> monitor) {

        Framework framework = Framework.getInstance();
        PcompParameters pcompParameters = new PcompParameters(
                PcompParameters.SharedSignalMode.OUTPUT, true, false);

        PcompTask pcompTask = new PcompTask(Arrays.asList(devStgFile, envStgFile),
                sysStgFile, detailFile, pcompParameters, directory);

        String description = "Running parallel composition [PComp]";
        SubtaskMonitor<Object> subtaskMonitor = null;
        if (monitor != null) {
            subtaskMonitor = new SubtaskMonitor<>(monitor);
        }
        TaskManager taskManager = framework.getTaskManager();
        return taskManager.execute(pcompTask, description, subtaskMonitor);
    }

}
