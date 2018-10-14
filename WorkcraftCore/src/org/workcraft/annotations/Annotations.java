package org.workcraft.annotations;

import org.workcraft.dom.references.Identifier;

public class Annotations {

    public static String getDisplayName(Class<?> cls) {
        DisplayName annotation = cls.getAnnotation(DisplayName.class);
        if (annotation == null) {
            return cls.getSimpleName();
        }
        return annotation.value();
    }

    public static String getShortName(Class<?> cls) {
        ShortName annotation = cls.getAnnotation(ShortName.class);
        if (annotation == null) {
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
        return annotation.value();
    }

    public static String getSVGIconPath(Class<?> cls) {
        SVGIcon annotation = cls.getAnnotation(SVGIcon.class);
        if (annotation == null) {
            return null;
        }
        return annotation.value();
    }

    public static int getHotKeyCode(Class<?> cls) {
        Hotkey annotation = cls.getAnnotation(Hotkey.class);
        if (annotation == null) {
            return -1;
        } else {
            return annotation.value();
        }
    }

    public static Class<?> getVisualClass(Class<?> cls) {
        VisualClass annotation = cls.getAnnotation(VisualClass.class);
        // The component/connection does not define a visual representation
        if (annotation == null) {
            return null;
        } else {
            return annotation.value();
        }
    }

    public static String getIentifierPrefix(Class<?> cls) {
        IdentifierPrefix annotation = cls.getAnnotation(IdentifierPrefix.class);
        if (annotation == null) {
            return null;
        }
        return annotation.isInternal() ? Identifier.createInternal(annotation.value()) : annotation.value();
    }

}
