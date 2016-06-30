package org.workcraft.dom.visual;

import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.workcraft.plugins.shared.CommonVisualSettings;

public class SizeHelper {

    private static final double FONT_SIZE = CommonVisualSettings.getFontSize();
    private static final double ICON_SCALE_THRESHOLD = 0.2;

    public static int getScreenDpi() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public static double getBaseSize() {
        return FONT_SIZE * getScreenDpi() / 72.0;
    }

    public static int getBaseFontSize() {
        return (int) Math.round(getBaseSize());
    }

    public static int getToolIconSize() {
        return (int) Math.round(2.0 * getBaseSize());
    }

    public static int getCheckBoxIconSize() {
        return (int) Math.round(0.9 * getBaseSize());
    }

    public static int getRadioBurronIconSize() {
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

    public static int getCompactLayoutHGap() {
        return (int) Math.round(0.2 * getBaseSize());
    }

    public static int getCompactLayoutVGap() {
        return (int) Math.round(0.2 * getBaseSize());
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

}
