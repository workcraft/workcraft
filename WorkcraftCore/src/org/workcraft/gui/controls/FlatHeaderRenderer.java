package org.workcraft.gui.controls;

import org.workcraft.dom.visual.SizeHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class FlatHeaderRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JTableHeader tableHeader = table.getTableHeader();
        if (tableHeader != null) {
            setForeground(tableHeader.getForeground());
            setBackground(tableHeader.getBackground());
        }
        setHorizontalAlignment(CENTER);
        setBorder(SizeHelper.getTableHeaderBorder());
        return this;
    }

}
