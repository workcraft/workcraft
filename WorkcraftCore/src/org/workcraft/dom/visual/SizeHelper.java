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
        return (int) Math.round(2.0 * getBaseFontSize());
    }

    public static int getRulerSize() {
        return (int) Math.round(1.1 * getBaseFontSize());
    }

    public static int getLayoutHGap() {
        return (int) Math.round(0.5 * getBaseFontSize());
    }

    public static int getLayoutVGap() {
        return (int) Math.round(0.5 * getBaseFontSize());
    }

    public static int getCompactLayoutHGap() {
        return (int) Math.round(0.2 * getBaseFontSize());
    }

    public static int getCompactLayoutVGap() {
        return (int) Math.round(0.2 * getBaseFontSize());
    }

    public static int getMonospacedFontSize() {
        return (int) Math.round(0.9 * getBaseFontSize());
    }

    public static int getComponentHeightFromFont(Font font) {
        return (int) Math.round(1.4 * font.getSize2D());
    }

}
