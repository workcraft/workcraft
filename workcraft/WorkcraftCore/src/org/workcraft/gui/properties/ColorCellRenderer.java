package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatComboBox;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ColorCellRenderer extends FlatComboBox implements TableCellRenderer {

    private static final Color PANEL_BACKGROUND = UIManager.getColor("Panel.background");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof Color) {
            setBackground((Color) value);
        }
        setOpaque(value == null);
        return this;
    }

    @Override
    public void paintComponent(Graphics g) {
        GuiUtils.paintBackgroundColor(g, getValueRectangle(),
                isOpaque() ? PANEL_BACKGROUND : getBackground());
    }

}
