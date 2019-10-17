package org.workcraft.dom.visual;

import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SizeHelper {

    private static final double ICON_SCALE_THRESHOLD = 0.2;
    private static final int WRAP_LENGTH = 100;

    public static int getWrapLength() {
        return WRAP_LENGTH;
    }

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

    public static int getBorderThickness() {
        return (int) Math.round(0.15 * getBaseSize());
    }

    public static int getMonospacedFontSize() {
        return (int) Math.round(0.9 * getBaseSize());
    }

    public static int getComponentHeightFromFont(Font font) {
        return (int) Math.round(1.4 * font.getSize2D());
    }

    private static Icon scaleButtonIcon(Icon icon, int size) {
        Icon result = icon;
        if (icon != null) {
            int h = icon.getIconHeight();
            if ((size > (1.0 + ICON_SCALE_THRESHOLD) * h) || (size < (1.0 - ICON_SCALE_THRESHOLD) * h)) {
                int w = icon.getIconWidth();
                BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                icon.paintIcon(new JButton(), image.getGraphics(), 0, 0);
                double ratio = (h == 0) ? 0.0 : (double) w / (double) h;
                int width = (int) Math.round(ratio * size);
                Image scaleImage = image.getScaledInstance(width, size, Image.SCALE_SMOOTH);
                result = new ImageIcon(scaleImage);
            }
        }
        return result;
    }

    public static Icon scaleFrameIcon(Icon icon) {
        return scaleButtonIcon(icon, getFrameButtonIconSize());
    }

    public static Icon scaleFileViewIcon(Icon icon) {
        return scaleButtonIcon(icon, getFileViewIconSize());
    }

    public static Icon scaleFileChooserIcon(Icon icon) {
        return scaleButtonIcon(icon, getFileChooserIconSize());
    }

    public static CompoundBorder getTitledBorder(String title) {
        return BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title), getEmptyBorder());
    }

    public static Border getEmptyBorder() {
        int gap = (int) Math.round(0.2 * getBaseSize());
        return BorderFactory.createEmptyBorder(gap, gap, gap, gap);
    }

    public static Border getTableCellBorder() {
        return BorderFactory.createEmptyBorder(1, 3, 1, 1);
    }

    public static Border getTableHeaderBorder() {
        return UIManager.getBorder("TableHeader.cellBorder");
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
