package org.workcraft.workspace;

import org.workcraft.dom.Model;
import org.workcraft.dom.math.MathModel;

public class WorkspaceUtils {

    public static boolean isApplicable(ModelEntry me, Class<?> cls) {
        return getAs(me, cls) != null;
    }

    public static boolean isApplicableExact(ModelEntry me, Class<?> cls) {
        boolean result = false;
        final Model model = me.getModel();
        if (model.getClass() == cls) {
            result = true;
        } else if (me.isVisual()) {
            final MathModel mathModel = me.getMathModel();
            result = mathModel.getClass() == cls;
        }
        return result;
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
