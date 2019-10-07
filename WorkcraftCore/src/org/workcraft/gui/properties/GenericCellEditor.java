package org.workcraft.gui.properties;

import org.workcraft.dom.visual.SizeHelper;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class GenericCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JTextField textField;

    public GenericCellEditor() {
        textField = new JTextField();
        textField.setFocusable(true);
        textField.setBorder(SizeHelper.getTableCellBorder());
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!e.isTemporary()) {
                    stopCellEditing();
                }
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        return textField.getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setText(value.toString());
        textField.setFont(table.getFont());
        return textField;
    }

}
