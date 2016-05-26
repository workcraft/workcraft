package org.workcraft.dom.visual;

import java.awt.Font;
import java.awt.Toolkit;

public class SizeHelper {
    private static final double FONT_SIZE = 10.0;

    public static int getDefaultFontSizeInPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(FONT_SIZE * screenDpi / 72.0);
    }

    public static int getDefaultIconSize() {
        return (int) Math.round(getDefaultFontSizeInPixels() * 2.0);
    }

    public static int getDefaultRulerSize() {
        return (int) Math.round(getDefaultFontSizeInPixels() * 1.1);
    }

    public static int getComponentHeightFromFont(Font font) {
        return (int) Math.round(font.getSize2D() * 1.4);
    }

}
