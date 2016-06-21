package org.workcraft.util;

import org.workcraft.dom.math.MathModel;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class WorkspaceUtils {

    public static boolean isApplicable(WorkspaceEntry entry, Class<?> cls) {
        return getAs(entry, cls) != null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAs(WorkspaceEntry we, Class<T> cls) {
        ModelEntry me = we.getModelEntry();
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
