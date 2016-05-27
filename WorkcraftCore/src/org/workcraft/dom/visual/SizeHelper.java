package org.workcraft.dom.visual;

import java.awt.Font;
import java.awt.Toolkit;

import org.workcraft.plugins.shared.CommonVisualSettings;

public class SizeHelper {

    private static final double FONT_SIZE = CommonVisualSettings.getFontSize();

    public static int getScreenDpi() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public static int getBaseFontSize() {
        return (int) Math.round(FONT_SIZE * getScreenDpi() / 72.0);
    }

    public static int getIconSize() {
        return (int) Math.round(getBaseFontSize() * 2.0);
    }

    public static int getRulerSize() {
        return (int) Math.round(getBaseFontSize() * 1.1);
    }

    public static int getLayoutVGap() {
        return (int) Math.round(getBaseFontSize() * 0.5);
    }

    public static int getLayoutHGap() {
        return (int) Math.round(getBaseFontSize() * 0.5);
    }

    public static int getComponentHeightFromFont(Font font) {
        return (int) Math.round(font.getSize2D() * 1.4);
    }

}
