package org.workcraft.gui.properties;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
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
        if (value != null) {
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
