package org.workcraft.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import org.workcraft.dom.visual.SizeHelper;

public class SilverOceanTheme extends OceanTheme {

    private final class CheckBoxIcon implements Icon {
        @Override
        public int getIconWidth() {
            // For symmetry the icon is better to have odd width
            return (SizeHelper.getCheckBoxIconSize() / 2) * 2 + 1;
        }

        @Override
        public int getIconHeight() {
            // For symmetry the icon is better to have odd height
            return (SizeHelper.getCheckBoxIconSize() / 2) * 2 + 1;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            AbstractButton abstractButton = (AbstractButton) c;
            ButtonModel buttonModel = abstractButton.getModel();
            int w = getIconWidth();
            int h = getIconHeight();
            if (g instanceof Graphics2D) {
                Graphics2D g2 = (Graphics2D) g;
                RenderingHints rh = new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHints(rh);
                float s = SizeHelper.getMinimalStrockWidth();
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

    private final class RadioButtonIcon implements Icon {
        @Override
        public int getIconWidth() {
            // For symmetry the icon is better to have odd width
            return (SizeHelper.getRadioBurronIconSize() / 2) * 2 + 1;
        }

        @Override
        public int getIconHeight() {
            // For symmetry the icon is better to have odd height
            return (SizeHelper.getRadioBurronIconSize() / 2) * 2 + 1;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            AbstractButton abstractButton = (AbstractButton) c;
            ButtonModel buttonModel = abstractButton.getModel();
            int w = getIconWidth();
            int h = getIconHeight();
            if (g instanceof Graphics2D) {
                Graphics2D g2 = (Graphics2D) g;
                RenderingHints rh = new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHints(rh);
                float s = SizeHelper.getMinimalStrockWidth();
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

    public static void enable() {
        MetalLookAndFeel.setCurrentTheme(new SilverOceanTheme());
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
        return  new ColorUIResource(0xcccccc);
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return  new ColorUIResource(0xeeeeee);
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
        Object[] buttonTable = getCustomButtonTable(table);
        Object[] gradientTable = getCustomGradientTable(table);
        Object[] iconTable = {};
        if (!DesktopApi.getOs().isMac()) {
            // FIXME: In OSX file/folder icons get corrupted and coloured close/min/max icons do not look right.
            iconTable = getCustomIconTable(table);
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

    private Object[] getCustomButtonTable(UIDefaults table) {
        Object[] result = {
                "TabbedPane.selected", getPrimary2(),
                "TabbedPane.contentAreaColor", getPrimary2(),

                "CheckBox.icon", new CheckBoxIcon(),
                "CheckBoxMenuItem.checkIcon", new CheckBoxIcon(),
                "RadioButton.icon", new RadioButtonIcon(),

                "ScrollBar.width", SizeHelper.getScrollbarWidth(),
        };
        return result;
    }

    private Object[] getCustomGradientTable(UIDefaults table) {
        List<Serializable> buttonGradient = Arrays.asList(1.0, 0.0, getSecondary3(), getSecondary2(), getSecondary2());

        Object[] result = {
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
        return result;
    }

    private Object[] getCustomIconTable(UIDefaults table) {
        Icon internalFrameIcon = UIManager.getIcon("InternalFrame.icon");
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

        Object[] result = {
                "InternalFrame.icon", SizeHelper.scaleFrameIcon(internalFrameIcon),
                "InternalFrame.minimizeIcon", SizeHelper.scaleFrameIcon(internalFrameMinimizeIcon),
                "InternalFrame.maximizeIcon", SizeHelper.scaleFrameIcon(internalFrameMaximizeIcon),
                "InternalFrame.closeIcon", SizeHelper.scaleFrameIcon(internalFrameCloseIcon),

                "FileView.computerIcon", SizeHelper.scaleFileViewIcon(fileViewComputerIcon),
                "FileView.directoryIcon", SizeHelper.scaleFileViewIcon(fileViewDirectoryIcon),
                "FileView.fileIcon", SizeHelper.scaleFileViewIcon(fileViewFileIcon),
                "FileView.floppyDriveIcon", SizeHelper.scaleFileViewIcon(fileViewFloppyDriveIcon),
                "FileView.hardDriveIcon", SizeHelper.scaleFileViewIcon(fileViewHardDriveIcon),

                "FileChooser.detailsViewIcon", SizeHelper.scaleFileChooserIcon(fileChooserDetailsViewIcon),
                "FileChooser.homeFolderIcon", SizeHelper.scaleFileChooserIcon(fileChooserHomeFolderIcon),
                "FileChooser.listViewIcon", SizeHelper.scaleFileChooserIcon(fileChooserListViewIcon),
                "FileChooser.newFolderIcon", SizeHelper.scaleFileChooserIcon(fileChooserNewFolderIcon),
                "FileChooser.upFolderIcon", SizeHelper.scaleFileChooserIcon(fileChooserUpFolderIcon),
        };
        return result;
    }

}
