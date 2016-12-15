package org.workcraft.workspace;

public class WorkspaceUtils {

    public static boolean isApplicable(ModelEntry me, Class<?> cls) {
        return (me != null) && me.isApplicable(cls);
    }

    public static boolean isApplicableExact(ModelEntry me, Class<?> cls) {
        return (me != null) && me.isApplicableExact(cls);
    }

    public static <T> T getAs(ModelEntry me, Class<T> cls) {
        return (me == null) ? null : me.getAs(cls);
    }

}
