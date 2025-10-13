package org.workcraft.annotations;

import org.workcraft.dom.references.Identifier;
import org.workcraft.utils.TextUtils;

public class Annotations {

    public static String getDisplayName(Class<?> cls) {
        DisplayName annotation = cls.getAnnotation(DisplayName.class);
        return annotation == null ? cls.getSimpleName() : annotation.value();
    }

    public static String getShortName(Class<?> cls) {
        ShortName annotation = cls.getAnnotation(ShortName.class);
        return annotation == null ? TextUtils.abbreviate(getDisplayName(cls)) : annotation.value();
    }

    public static String getSVGIconPath(Class<?> cls) {
        SVGIcon annotation = cls.getAnnotation(SVGIcon.class);
        return annotation == null ? null : annotation.value();
    }

    public static int getHotKeyCode(Class<?> cls) {
        Hotkey annotation = cls.getAnnotation(Hotkey.class);
        return annotation == null ? -1 : annotation.value();
    }

    public static Class<?> getVisualClass(Class<?> cls) {
        VisualClass annotation = cls.getAnnotation(VisualClass.class);
        // The component/connection does not define a visual representation
        return annotation == null ? null : annotation.value();
    }

    public static String getIdentifierPrefix(Class<?> cls) {
        IdentifierPrefix annotation = cls.getAnnotation(IdentifierPrefix.class);
        return annotation == null ? null : annotation.isInternal()
                ? Identifier.addInternalPrefix(annotation.value()) : annotation.value();
    }

}
