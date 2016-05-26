package org.workcraft.dom.visual;

import java.awt.Font;
import java.awt.Toolkit;

public class SizeHelper {
    private static final double FONT_SIZE = 10.0;
    private static final int SCREEN_DPI = Toolkit.getDefaultToolkit().getScreenResolution();
    private static final int REF_SIZE = (int) Math.round(FONT_SIZE * SCREEN_DPI / 72.0);

    public static int getDefaultIconSize() {
        return (int) Math.round(REF_SIZE * 2.0);
    }

    public static int getDefaultRulerSize() {
        return (int) Math.round(REF_SIZE * 1.1);
    }

    public static int getLayoutVGap() {
        return (int) Math.round(REF_SIZE * 0.5);
    }

    public static int getLayoutHGap() {
        return (int) Math.round(REF_SIZE * 0.5);
    }

    public static int getComponentHeightFromFont(Font font) {
        return (int) Math.round(font.getSize2D() * 1.4);
    }

}
