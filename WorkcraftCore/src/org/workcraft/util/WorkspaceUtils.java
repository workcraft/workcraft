package org.workcraft.util;

import org.workcraft.dom.math.MathModel;
import org.workcraft.workspace.WorkspaceEntry;

public class WorkspaceUtils {
    public static boolean canHas(WorkspaceEntry entry, Class<?> cls) {
        return getAs(entry, cls) != null;
    }
    @SuppressWarnings("unchecked")
    public static <T> T getAs(WorkspaceEntry entry, Class<T> cls) {
        if (cls.isInstance(entry.getModelEntry().getModel())) {
            return (T) entry.getModelEntry().getModel();
        }

        if (entry.getModelEntry().isVisual()) {
            final MathModel mathModel = entry.getModelEntry().getMathModel();
            if (cls.isInstance(mathModel)) {
                return (T) mathModel;
            }
        }
        return null;
    }
}
