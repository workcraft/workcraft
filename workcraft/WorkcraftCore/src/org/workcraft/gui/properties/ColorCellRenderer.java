package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatComboBox;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

@SuppressWarnings("serial")
public class ColorCellRenderer extends FlatComboBox implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof Color) {
            setBackground((Color) value);
        }
        setOpaque(value == null);
        return this;
    }

}
