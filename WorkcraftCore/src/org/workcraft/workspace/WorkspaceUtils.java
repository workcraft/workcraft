package org.workcraft.workspace;

public class WorkspaceUtils {

    public static boolean isApplicable(WorkspaceEntry we, Class<?> cls) {
        boolean result = false;
        if (we != null) {
            ModelEntry me = we.getModelEntry();
            if (me != null) {
                result = me.isApplicable(cls);
            }
        }
        return result;
    }

    public static boolean isApplicableExact(WorkspaceEntry we, Class<?> cls) {
        boolean result = false;
        if (we != null) {
            ModelEntry me = we.getModelEntry();
            if (me != null) {
                result = me.isApplicableExact(cls);
            }
        }
        return result;
    }

    public static <T> T getAs(WorkspaceEntry we, Class<T> cls) {
        T result = null;
        if (we != null) {
            ModelEntry me = we.getModelEntry();
            if (me != null) {
                result = me.getAs(cls);
            }
        }
        return result;
    }

}
