package org.workcraft.gui.tools;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public final class ColorDataRenderer implements TableCellRenderer {

    private final JLabel label = new JLabel() {
        @Override
        public void paint(final Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paint(g);
        }
    };

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value,
            final boolean isSelected, final boolean hasFocus, final int row, final int col) {

        label.setText("");
        label.setBorder(GuiUtils.getTableCellBorder());
        label.setBackground((Color) value);
        return label;
    }

}
