package org.workcraft.utils;

import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;

public class ValidationUtils {

    public static <T> void validateMathModelType(MathModel mathModel,
            Class<T> expectedMathModelClass, String visualModelClassName)
            throws VisualModelInstantiationException {

        if (!expectedMathModelClass.isAssignableFrom(mathModel.getClass())) {
            String mathModelClassName = mathModel.getClass().getSimpleName();
            String msg = "Math model class '" + mathModelClassName + "' is unsupported";
            if ((visualModelClassName != null) && !visualModelClassName.isEmpty()) {
                msg += " by the visual model class '" + visualModelClassName + "'";
            }
            msg += ".";
            throw new VisualModelInstantiationException(msg);
        }
    }

}
