package org.workcraft.gui.controls;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class FlatHeaderRenderer extends DefaultTableCellRenderer {

    private final boolean tooltipLongText;

    private final JLabel label = new JLabel() {
        @Override
        public void paint(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paint(g);
        }
    };

    public FlatHeaderRenderer() {
        this(true);
    }

    public FlatHeaderRenderer(boolean tooltipLongText) {
        this.tooltipLongText = tooltipLongText;
        label.setBorder(GuiUtils.getTableHeaderBorder());
        label.setHorizontalAlignment(CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {

        JTableHeader tableHeader = table.getTableHeader();
        if (tableHeader != null) {
            label.setForeground(tableHeader.getForeground());
            label.setBackground(tableHeader.getBackground());
        }

        label.setText(value == null ? "" : value.toString());
        if (tooltipLongText) {
            boolean fits = GuiUtils.getLabelTextWidth(label) < GuiUtils.getTableColumnTextWidth(table, col);
            label.setToolTipText(fits ? null : label.getText());
        }
        return label;
    }

}
