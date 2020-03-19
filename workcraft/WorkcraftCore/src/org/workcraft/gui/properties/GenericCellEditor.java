package org.workcraft.gui.properties;

import org.workcraft.dom.visual.SizeHelper;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

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
        textField.setFont(table.getFont());
        textField.setText(value.toString());
        return textField;
    }

}
