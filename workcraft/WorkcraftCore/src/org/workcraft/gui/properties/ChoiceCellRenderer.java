package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatComboBox;

import java.awt.*;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ChoiceCellRenderer extends FlatComboBox implements TableCellRenderer {

    public ChoiceCellRenderer() {
        super();
        setEditable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value != null) {
            addItem(value);
        }
        setOpaque(value == null);
        return this;
    }

}
