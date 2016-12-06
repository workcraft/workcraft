package org.workcraft.util;

import org.workcraft.dom.math.MathModel;
import org.workcraft.workspace.ModelEntry;

public class WorkspaceUtils {

    public static boolean isApplicable(ModelEntry me, Class<?> cls) {
        return getAs(me, cls) != null;
    }

    public static boolean isApplicableExact(ModelEntry me, Class<?> cls) {
        return me.getMathModel().getClass().equals(cls.getName()); // !!!
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAs(ModelEntry me, Class<T> cls) {
        if (cls.isInstance(me.getModel())) {
            return (T) me.getModel();
        }
        if (me.isVisual()) {
            final MathModel mathModel = me.getMathModel();
            if (cls.isInstance(mathModel)) {
                return (T) mathModel;
            }
        }
        return null;
    }

}
