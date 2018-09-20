package org.workcraft.annotations;

public class Annotations {

    public static String getDisplayName(Class<?> cls) {
        DisplayName dn = cls.getAnnotation(DisplayName.class);
        if (dn == null) {
            return cls.getSimpleName();
        }
        return dn.value();
    }

    public static String getShortName(Class<?> cls) {
        ShortName sn = cls.getAnnotation(ShortName.class);
        if (sn == null) {
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
        return sn.value();
    }

    public static String getSVGIconPath(Class<?> cls) {
        SVGIcon icon = cls.getAnnotation(SVGIcon.class);
        if (icon == null) {
            return null;
        }
        return icon.value();
    }

    public static int getHotKeyCode(Class<?> cls) {
        Hotkey hkd = cls.getAnnotation(Hotkey.class);
        if (hkd == null) {
            return -1;
        } else {
            return hkd.value();
        }
    }

    public static Class<?> getVisualClass(Class<?> cls) {
        VisualClass vcat = cls.getAnnotation(VisualClass.class);
        // The component/connection does not define a visual representation
        if (vcat == null) {
            return null;
        } else {
            return vcat.value();
        }
    }

}
