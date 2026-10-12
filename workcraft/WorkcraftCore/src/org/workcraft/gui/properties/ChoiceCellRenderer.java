package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatComboBox;

import java.awt.*;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ChoiceCellRenderer extends FlatComboBox implements TableCellRenderer {

    public ChoiceCellRenderer() {
        super();
        setEditable(false);
        setButtonStyle(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        removeAllItems();
        if (value != null) {
            addItem(value);
        }
        setSelectedItem(value);
        setFont(table.getFont());
        boolean isEnabled = table.isEnabled() && table.isCellEditable(row, column);
        setEnabled(isEnabled);
        return this;
    }

}
