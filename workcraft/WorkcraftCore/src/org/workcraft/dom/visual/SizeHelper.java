package org.workcraft.dom.visual;

import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

import java.awt.*;

public class SizeHelper {

    public static double getScreenDpi() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public static double getScreenDpmm() {
        return getScreenDpi() * 0.0393701;
    }

    public static double getBaseSize() {
        return VisualCommonSettings.getFontSize() * getScreenDpi() / 72.0;
    }

    public static int getBaseFontSize() {
        return (int) Math.round(getBaseSize());
    }

    public static int getPreviewFontSize() {
        return (int) Math.round(1.5 * getBaseSize());
    }

    public static int getIconSize() {
        return 32;
    }

    public static int getToolIconSize() {
        return (int) Math.round(2.0 * getBaseSize());
    }

    public static int getListRowSize() {
        return (int) Math.round(1.8 * getBaseSize());
    }

    public static int getCheckBoxIconSize() {
        return (int) Math.round(0.9 * getBaseSize());
    }

    public static int getRadioButtonIconSize() {
        return (int) Math.round(0.9 * getBaseSize());
    }

    public static int getFrameButtonIconSize() {
        return (int) Math.round(1.2 * getBaseSize());
    }

    public static int getFileViewIconSize() {
        return (int) Math.round(getBaseSize());
    }

    public static int getFileChooserIconSize() {
        return (int) Math.round(1.3 * getBaseSize());
    }

    public static int getScrollbarWidth() {
        return (int) Math.round(1.3 * getBaseSize());
    }

    public static int getColorChooserSwatchSize() {
        return (int) Math.round(1.05 * getBaseSize());
    }

    public static int getColorChooserSwatchRecentSize() {
        return (int) Math.round(9.0 * getColorChooserSwatchSize() / 7.0);
    }

    public static float getMinimalStrockWidth() {
        return (float) (0.1 * getBaseSize());
    }

    public static int getRulerSize() {
        return (int) Math.round(1.1 * getBaseSize());
    }

    public static int getLayoutHGap() {
        return (int) Math.round(0.5 * getBaseSize());
    }

    public static int getLayoutVGap() {
        return (int) Math.round(0.5 * getBaseSize());
    }

    public static int getCellHGap() {
        return (int) Math.round(0.3 * getBaseSize());
    }

    public static int getCellVGap() {
        return (int) Math.round(0.1 * getBaseSize());
    }

    public static int getBorderThickness() {
        return (int) Math.round(0.15 * getBaseSize());
    }

    public static int getMonospacedFontSize() {
        return (int) Math.round(0.9 * getBaseSize());
    }

    public static int getComponentHeightFromFont(Font font) {
        return (int) Math.round(1.4 * font.getSize2D());
    }

    public static Insets getTextMargin() {
        int gap = (int) Math.round(0.2 * getBaseSize());
        return new Insets(gap, gap, gap, gap);
    }

    public static Insets getTreeCheckboxMargin() {
        int gap = (int) Math.round(0.2 * getBaseSize());
        return new Insets(0, 0, 0, gap);
    }

}
