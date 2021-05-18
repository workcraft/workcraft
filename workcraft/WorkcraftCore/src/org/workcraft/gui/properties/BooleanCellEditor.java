package org.workcraft.gui.properties;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class BooleanCellEditor extends AbstractCellEditor implements TableCellEditor, ItemListener {

    private final JCheckBox checkBox;

    public BooleanCellEditor() {
        checkBox = new JCheckBox();
        checkBox.setFocusable(false);
        checkBox.addItemListener(this);
        checkBox.setBorderPainted(false);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof Boolean) {
            checkBox.setSelected((Boolean) value);
        }
        checkBox.setOpaque(value == null);
        checkBox.setFont(table.getFont());
        return checkBox;
    }

    @Override
    public Boolean getCellEditorValue() {
        return checkBox.isSelected();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        fireEditingStopped();
    }

}
