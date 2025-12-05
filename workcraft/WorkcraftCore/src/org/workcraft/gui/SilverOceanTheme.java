package org.workcraft.gui;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.DesktopApi;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class SilverOceanTheme extends OceanTheme {

    private static final double ICON_SCALE_THRESHOLD = 0.2;

    private static final class CheckBoxIcon implements Icon {
        @Override
        public int getIconWidth() {
            // For symmetry the icon is better to have odd width
            return SizeHelper.getCheckBoxIconSize() / 2 * 2 + 1;
        }

        @Override
        public int getIconHeight() {
            // For symmetry the icon is better to have odd height
            return SizeHelper.getCheckBoxIconSize() / 2 * 2 + 1;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            AbstractButton abstractButton = (AbstractButton) c;
            ButtonModel buttonModel = abstractButton.getModel();
            int w = getIconWidth();
            int h = getIconHeight();
            if (g instanceof Graphics2D g2) {
                RenderingHints rh = new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHints(rh);
                float s = SizeHelper.getMinimalStrokeWidth();
                g2.setStroke(new BasicStroke(s));
            }
            g.setColor(Color.WHITE);
            g.fillRect(x, y, w, h);
            if (buttonModel.isEnabled()) {
                g.setColor(Color.BLACK);
            } else {
                g.setColor(Color.GRAY);
            }
            g.drawRect(x, y, w, h);
            if (buttonModel.isSelected()) {
                g.drawLine(x + 1, y + 1, x + w - 1, y + h - 1);
                g.drawLine(x + w - 1, y + 1, x + 1, y + h - 1);
            }
        }
    }

    private static final class RadioButtonIcon implements Icon {
        @Override
        public int getIconWidth() {
            // For symmetry the icon is better to have odd width
            return SizeHelper.getRadioButtonIconSize() / 2 * 2 + 1;
        }

        @Override
        public int getIconHeight() {
            // For symmetry the icon is better to have odd height
            return SizeHelper.getRadioButtonIconSize() / 2 * 2 + 1;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            AbstractButton abstractButton = (AbstractButton) c;
            ButtonModel buttonModel = abstractButton.getModel();
            int w = getIconWidth();
            int h = getIconHeight();
            if (g instanceof Graphics2D g2) {
                RenderingHints rh = new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHints(rh);
                float s = SizeHelper.getMinimalStrokeWidth();
                g2.setStroke(new BasicStroke(s));
            }
            g.setColor(Color.WHITE);
            g.fillOval(x, y, w - 1, h - 1);
            if (buttonModel.isEnabled()) {
                g.setColor(Color.BLACK);
            } else {
                g.setColor(Color.GRAY);
            }
            g.drawOval(x, y, w - 1, h - 1);
            if (buttonModel.isSelected()) {
                int dx = (int) Math.round(0.25 * w);
                int dy = (int) Math.round(0.25 * h);
                g.fillOval(x + dx, y + dy, w - 2 * dx, h - 2 * dy);
            }
        }
    }

    @Override
    public FontUIResource getControlTextFont() {
        return new FontUIResource(Font.SANS_SERIF, Font.PLAIN, SizeHelper.getBaseFontSize());
    }

    @Override
    public FontUIResource getMenuTextFont() {
        return getControlTextFont();
    }

    @Override
    public FontUIResource getSubTextFont() {
        return getControlTextFont();
    }

    @Override
    public FontUIResource getSystemTextFont() {
        return getControlTextFont();
    }

    @Override
    public FontUIResource getUserTextFont() {
        return getControlTextFont();
    }

    @Override
    public FontUIResource getWindowTitleFont() {
        return new FontUIResource(getControlTextFont().deriveFont(Font.BOLD));
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return new ColorUIResource(0x999999);
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return new ColorUIResource(0xcccccc);
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return new ColorUIResource(0xeeeeee);
    }

    @Override
    protected ColorUIResource getPrimary1() {
        return new ColorUIResource(0x999999);
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return new ColorUIResource(0xbbccdd);
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return new ColorUIResource(0xbbccdd);
    }

    @Override
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
        Object[] buttonTable = getCustomButtonTable();
        Object[] gradientTable = getCustomGradientTable();
        Object[] iconTable = {};
        if (!DesktopApi.getOs().isMac()) {
            // FIXME: In OSX file/folder icons get corrupted and coloured close/min/max icons do not look right.
            iconTable = getCustomIconTable();
        }
        Object[] uiFullTable = mergeTables(buttonTable, gradientTable, iconTable);
        table.putDefaults(uiFullTable);

    }

    private Object[] mergeTables(Object[]... tables) {
        int length = 0;
        for (Object[] table: tables) {
            length += table.length;
        }
        Object[] result = new Object[length];
        int pos = 0;
        for (Object[] table: tables) {
            System.arraycopy(table, 0, result, pos, table.length);
            pos += table.length;
        }
        return result;
    }

    private Object[] getCustomButtonTable() {
        int swatchSize = SizeHelper.getColorChooserSwatchSize();
        int swatchRecentSize = SizeHelper.getColorChooserSwatchRecentSize();
        return new Object[]{
                "TabbedPane.selected", getPrimary2(),
                "TabbedPane.contentAreaColor", getPrimary2(),

                "CheckBox.icon", new CheckBoxIcon(),
                "CheckBoxMenuItem.checkIcon", new CheckBoxIcon(),
                "RadioButton.icon", new RadioButtonIcon(),

                "ComboBox.background", getWhite(),

                "ScrollBar.width", SizeHelper.getScrollbarWidth(),

                "ColorChooser.swatchesSwatchSize", new Dimension(swatchSize, swatchSize),
                "ColorChooser.swatchesRecentSwatchSize", new Dimension(swatchRecentSize, swatchRecentSize),
        };
    }

    private Object[] getCustomGradientTable() {
        List<Serializable> buttonGradient = Arrays.asList(1.0, 0.0, getSecondary3(), getSecondary2(), getSecondary2());
        return new Object[]{
                "Button.gradient", buttonGradient,
                "CheckBox.gradient", buttonGradient,
                "CheckBoxMenuItem.gradient", buttonGradient,
                "InternalFrame.activeTitleGradient", buttonGradient,
                "RadioButton.gradient", buttonGradient,
                "RadioButtonMenuItem.gradient", buttonGradient,
                "ScrollBar.gradient", buttonGradient,
                "Slider.focusGradient", buttonGradient,
                "Slider.gradient", buttonGradient,
                "ToggleButton.gradient", buttonGradient,
        };
    }

    private Object[] getCustomIconTable() {
        Icon internalFrameIcon = UIManager.getIcon("InternalFrame.icon");
        Icon internalFrameIconifyIcon = UIManager.getIcon("InternalFrame.iconifyIcon");
        Icon internalFrameMinimizeIcon = UIManager.getIcon("InternalFrame.minimizeIcon");
        Icon internalFrameMaximizeIcon = UIManager.getIcon("InternalFrame.maximizeIcon");
        Icon internalFrameCloseIcon = UIManager.getIcon("InternalFrame.closeIcon");

        Icon fileViewComputerIcon = UIManager.getIcon("FileView.computerIcon");
        Icon fileViewDirectoryIcon = UIManager.getIcon("FileView.directoryIcon");
        Icon fileViewFileIcon = UIManager.getIcon("FileView.fileIcon");
        Icon fileViewFloppyDriveIcon = UIManager.getIcon("FileView.floppyDriveIcon");
        Icon fileViewHardDriveIcon = UIManager.getIcon("FileView.hardDriveIcon");

        Icon fileChooserDetailsViewIcon = UIManager.getIcon("FileChooser.detailsViewIcon");
        Icon fileChooserHomeFolderIcon = UIManager.getIcon("FileChooser.homeFolderIcon");
        Icon fileChooserListViewIcon = UIManager.getIcon("FileChooser.listViewIcon");
        Icon fileChooserNewFolderIcon = UIManager.getIcon("FileChooser.newFolderIcon");
        Icon fileChooserUpFolderIcon = UIManager.getIcon("FileChooser.upFolderIcon");

        return new Object[]{
                "InternalFrame.icon", scaleFrameIcon(internalFrameIcon),
                "InternalFrame.iconifyIcon", scaleFrameIcon(internalFrameIconifyIcon),
                "InternalFrame.minimizeIcon", scaleFrameIcon(internalFrameMinimizeIcon),
                "InternalFrame.maximizeIcon", scaleFrameIcon(internalFrameMaximizeIcon),
                "InternalFrame.closeIcon", scaleFrameIcon(internalFrameCloseIcon),

                "FileView.computerIcon", scaleFileViewIcon(fileViewComputerIcon),
                "FileView.directoryIcon", scaleFileViewIcon(fileViewDirectoryIcon),
                "FileView.fileIcon", scaleFileViewIcon(fileViewFileIcon),
                "FileView.floppyDriveIcon", scaleFileViewIcon(fileViewFloppyDriveIcon),
                "FileView.hardDriveIcon", scaleFileViewIcon(fileViewHardDriveIcon),

                "FileChooser.detailsViewIcon", scaleFileChooserIcon(fileChooserDetailsViewIcon),
                "FileChooser.homeFolderIcon", scaleFileChooserIcon(fileChooserHomeFolderIcon),
                "FileChooser.listViewIcon", scaleFileChooserIcon(fileChooserListViewIcon),
                "FileChooser.newFolderIcon", scaleFileChooserIcon(fileChooserNewFolderIcon),
                "FileChooser.upFolderIcon", scaleFileChooserIcon(fileChooserUpFolderIcon),
        };
    }

    private static Icon scaleButtonIcon(Icon icon, int size) {
        Icon result = icon;
        if (icon != null) {
            int h = icon.getIconHeight();
            if ((size > (1.0 + ICON_SCALE_THRESHOLD) * h) || (size < (1.0 - ICON_SCALE_THRESHOLD) * h)) {
                int w = icon.getIconWidth();
                BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                icon.paintIcon(new JButton(), image.getGraphics(), 0, 0);
                double ratio = (h == 0) ? 0.0 :  (double) w / h;
                int width = (int) Math.round(ratio * size);
                Image scaleImage = image.getScaledInstance(width, size, Image.SCALE_SMOOTH);
                result = new ImageIcon(scaleImage);
            }
        }
        return result;
    }

    private static Icon scaleFrameIcon(Icon icon) {
        return scaleButtonIcon(icon, SizeHelper.getFrameButtonIconSize());
    }

    private static Icon scaleFileViewIcon(Icon icon) {
        return scaleButtonIcon(icon, SizeHelper.getFileViewIconSize());
    }

    private static Icon scaleFileChooserIcon(Icon icon) {
        return scaleButtonIcon(icon, SizeHelper.getFileChooserIconSize());
    }

}
