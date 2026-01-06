package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.panels.WorkspacePanel;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

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

    public static void pauseWorkspaceViewAutoExpansion() {
        setWorkspaceViewAutoExpansion(false);
    }

    public static void resumeWorkspaceViewAutoExpansion() {
        setWorkspaceViewAutoExpansion(true);
    }

    private static void setWorkspaceViewAutoExpansion(boolean value) {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        WorkspacePanel workspaceView = mainWindow == null ? null : mainWindow.getWorkspaceView();
        if (workspaceView != null) {
            workspaceView.setAutoExpand(value);
            workspaceView.refresh();
        }
    }

}
