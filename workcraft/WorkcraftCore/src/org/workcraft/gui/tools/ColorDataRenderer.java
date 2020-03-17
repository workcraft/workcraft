package org.workcraft.gui.tools;

import org.workcraft.dom.visual.SizeHelper;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public final class ColorDataRenderer implements TableCellRenderer {

    private final JLabel label = new JLabel() {
        @Override
        public void paint(final Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
            super.paint(g);
        }
    };

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value,
            final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        label.setText("");
        label.setBorder(SizeHelper.getTableCellBorder());
        label.setBackground((Color) value);
        return label;
    }

}
