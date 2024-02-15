package org.workcraft.plugins.pcomp.utils;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PcompUtils {

    public static List<WorkspaceEntry> deserialiseData(String data) {
        List<WorkspaceEntry> wes = new ArrayList<>();
        Framework framework = Framework.getInstance();
        StringBuilder msg = new StringBuilder();
        for (String word : TextUtils.splitWords(data)) {
            try {
                WorkspaceEntry we = framework.loadWork(word);
                if (we == null) {
                    msg.append("\n  * ").append(word);
                } else {
                    wes.add(we);
                }
            } catch (DeserialisationException e) {
                msg.append("\n  * ").append(word);
            }
        }
        if (msg.length() > 0) {
            wes.clear();
            LogUtils.logError("Could not load the following files:" + msg);
        }
        return wes;
    }

    public static Result<? extends PcompOutput> composeDevWithEnv(File devStgFile, File envStgFile,
            File directory, ProgressMonitor<?> monitor) {

        String sysStgFileName = StgUtils.SYSTEM_FILE_PREFIX + StgFormat.getInstance().getExtension();
        String detailFileName = PcompTask.DETAIL_FILE_PREFIX + PcompTask.DETAIL_FILE_EXTENSION;
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
