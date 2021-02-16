package org.workcraft.gui.properties;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class PropertyDeclarationRenderer extends DefaultTableCellRenderer {

    private final JLabel label = new JLabel() {
        @Override
        public void paint(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paint(g);
        }
    };

    public PropertyDeclarationRenderer(PropertyDescriptor descriptor) {
        label.setBorder(GuiUtils.getTableCellBorder());
        label.setHorizontalAlignment(LEADING);

        if ((descriptor != null) && (descriptor.getValue() == null) && descriptor.isCombinable()) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {

        JTableHeader tableHeader = table.getTableHeader();
        if (tableHeader != null) {
            label.setForeground(tableHeader.getForeground());
            label.setBackground(tableHeader.getBackground());
        }

        label.setText((value == null) ? "" : value.toString());

        boolean fits = GuiUtils.getLabelTextWidth(label) < GuiUtils.getTableColumnTextWidth(table, col);
        label.setToolTipText(fits ? null : label.getText());
        return label;
    }

}
