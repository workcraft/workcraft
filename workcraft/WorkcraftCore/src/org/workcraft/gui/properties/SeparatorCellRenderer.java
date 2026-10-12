package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatLabel;
import org.workcraft.utils.ColorUtils;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class SeparatorCellRenderer implements TableCellRenderer {

    // Row height relative to the normal one, to separate sections with some whitespace
    public static final double ROW_HEIGHT_FACTOR = 1.3;

    private final JLabel label = new FlatLabel() {
        @Override
        public void paint(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paint(g);
        }
    };

    public SeparatorCellRenderer() {
        label.setVerticalAlignment(SwingConstants.BOTTOM);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        // Use the colors of the (non-editable) property name column, with a darker background
        JTableHeader tableHeader = table.getTableHeader();
        if (tableHeader != null) {
            label.setForeground(tableHeader.getForeground());
            label.setBackground(ColorUtils.fade(tableHeader.getBackground(), 0.8));
        }
        label.setFont(table.getFont().deriveFont(Font.BOLD));
        label.setText(value == null ? "" : value.toString());
        return label;
    }

}
