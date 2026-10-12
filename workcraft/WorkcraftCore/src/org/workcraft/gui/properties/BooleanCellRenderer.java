package org.workcraft.gui.properties;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class BooleanCellRenderer extends JCheckBox implements TableCellRenderer {

    public BooleanCellRenderer() {
        setFocusable(false);
        setBorderPainted(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Boolean boolValue) {
            setSelected(boolValue);
        }
        setOpaque(value == null);
        boolean isEnabled = table.isEnabled() && table.isCellEditable(row, column);
        setEnabled(isEnabled);
        return this;
    }

}
