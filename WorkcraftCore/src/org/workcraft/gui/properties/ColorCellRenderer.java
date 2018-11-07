package org.workcraft.gui.properties;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class ColorCellRenderer extends FlatComboBox implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value != null) {
            setBackground((Color) value);
        }
        setOpaque(value == null);
        return this;
    }

}
