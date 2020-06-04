package org.workcraft.annotations;

import org.workcraft.dom.references.Identifier;

public class Annotations {

    public static String getDisplayName(Class<?> cls) {
        DisplayName annotation = cls.getAnnotation(DisplayName.class);
        return annotation == null ? cls.getSimpleName() : annotation.value();
    }

    public static String getShortName(Class<?> cls) {
        ShortName annotation = cls.getAnnotation(ShortName.class);
        return annotation == null ? getShortNameFromDisplayName(cls) : annotation.value();
    }

    private static String getShortNameFromDisplayName(Class<?> cls) {
        String result = "";
        String s = getDisplayName(cls);
        boolean b = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (b && !Character.isSpaceChar(c) || Character.isUpperCase(c)) {
                result += c;
            }
            b = Character.isSpaceChar(c);
        }
        return result;
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
                ? Identifier.makeInternal(annotation.value()) : annotation.value();
    }

}
