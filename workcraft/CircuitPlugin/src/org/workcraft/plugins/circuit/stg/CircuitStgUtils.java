package org.workcraft.plugins.circuit.stg;

import org.workcraft.Framework;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.TaskManager;

import java.io.File;
import java.util.Arrays;

public class CircuitStgUtils {

    public static Result<? extends PcompOutput> composeDevWithEnv(File devStgFile, File envStgFile,
            File directory, ProgressMonitor<?> monitor) {

        String sysStgFileName = StgUtils.SYSTEM_FILE_PREFIX + StgFormat.getInstance().getExtension();
        String detailFileName = StgUtils.DETAIL_FILE_PREFIX + StgUtils.XML_FILE_EXTENSION;
        return composeDevWithEnv(devStgFile, envStgFile, directory, monitor, sysStgFileName, detailFileName);
    }

    public static Result<? extends PcompOutput> composeDevWithEnv(File devStgFile, File envStgFile,
            File directory, ProgressMonitor<?> monitor, String sysStgFileName, String detailFileName) {

        PcompParameters pcompParameters = new PcompParameters(
                PcompParameters.SharedSignalMode.OUTPUT, true, false);

        PcompTask pcompTask = new PcompTask(Arrays.asList(devStgFile, envStgFile), pcompParameters,
                directory, sysStgFileName, detailFileName);

        String description = "Running parallel composition [PComp]";
        SubtaskMonitor<Object> subtaskMonitor = monitor == null ? null : new SubtaskMonitor<>(monitor);
        TaskManager taskManager = Framework.getInstance().getTaskManager();
        return taskManager.execute(pcompTask, description, subtaskMonitor);
    }

}
