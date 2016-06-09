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

    public static int getScreenDpi() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public static double getBaseSize() {
        return FONT_SIZE * getScreenDpi() / 72.0;
    }

    public static int getBaseFontSize() {
        return (int) Math.round(getBaseSize());
    }

    public static int getIconSize() {
        return (int) Math.round(2.0 * getBaseSize());
    }

    public static int getCheckBoxIconSize() {
        return (int) Math.round(0.45 * getBaseSize()) * 2 + 1;
    }

    public static int getRadioBurronIconSize() {
        return (int) Math.round(0.45 * getBaseSize()) * 2 + 1;
    }

    public static int getFrameButtonIconSize() {
        return (int) Math.round(0.5 * getBaseSize()) * 2 + 1;
    }

    public static int getFileViewIconSize() {
        return (int) Math.round(0.5 * getBaseSize()) * 2 + 1;
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

    public static Icon scaleFrameIcon(Icon icon) {
        Icon result = icon;
        if (icon != null) {
            int size = getFrameButtonIconSize();
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            if (size > 1.2 * h) {
                BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                icon.paintIcon(new JButton(), image.getGraphics(), 0, 0);
                Image scaleImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                result = new ImageIcon(scaleImage);
            }
        }
        return result;
    }

    public static Icon scaleFileViewIcon(Icon icon) {
        Icon result = icon;
        if (icon != null) {
            int size = getFileViewIconSize();
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            if (size > 1.2 * h) {
                BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                icon.paintIcon(new JButton(), image.getGraphics(), 0, 0);
                Image scaleImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                result = new ImageIcon(scaleImage);
            }
        }
        return result;
    }

}
