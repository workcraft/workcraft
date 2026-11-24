package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.panels.WorkspacePanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class WorkspaceUtils {

    public static boolean isApplicable(WorkspaceEntry we, Class<? extends Model<?, ?>> cls) {
        if (we != null) {
            return isApplicable(we.getModelEntry(), cls);
        }
        return false;
    }

    public static boolean isApplicable(ModelEntry me, Class<?> cls) {
        if (me != null) {
            return me.isApplicable(cls);
        }
        return false;
    }

    public static boolean isApplicableExact(WorkspaceEntry we, Class<? extends Model<?, ?>> cls) {
        if (we != null) {
            return isApplicableExact(we.getModelEntry(), cls);
        }
        return false;
    }

    public static boolean isApplicableExact(ModelEntry me, Class<? extends Model<?, ?>> cls) {
        if (me != null) {
            return me.isApplicableExact(cls);
        }
        return false;
    }

    public static <T> T getAs(WorkspaceEntry we, Class<T> cls) {
        T result = null;
        if (we != null) {
            result = getAs(we.getModelEntry(), cls);
        }
        return result;
    }

    public static <T> T getAs(ModelEntry me, Class<T> cls) {
        T result = null;
        if (me != null) {
            result = me.getAs(cls);
        }
        return result;
    }

    /**
     * Pause updates of the workspace view while works are loaded, saved and closed in a sequence (e.g. by a task),
     * so that it does not flicker. Each call must be paired with {@link #resumeWorkspaceViewUpdates()}, e.g. in a
     * finally block.
     */
    public static void pauseWorkspaceViewUpdates() {
        WorkspacePanel workspaceView = getWorkspaceView();
        if (workspaceView != null) {
            workspaceView.pauseUpdates();
        }
    }

    public static void resumeWorkspaceViewUpdates() {
        WorkspacePanel workspaceView = getWorkspaceView();
        if (workspaceView != null) {
            workspaceView.resumeUpdates();
        }
    }

    private static WorkspacePanel getWorkspaceView() {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        return (mainWindow == null) ? null : mainWindow.getWorkspacePanel();
    }

    public static boolean closeFileIfOpen(File file) {
        Framework framework = Framework.getInstance();
        Workspace workspace = framework.getWorkspace();
        Path<String> path = workspace.getPath(file);
        WorkspaceEntry we = workspace.getWork(path);
        boolean isOpen = (we != null);
        if (isOpen) {
            framework.closeWork(we);
            workspace.removeMount(path);
        }
        return isOpen;
    }

}
