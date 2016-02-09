package org.workcraft.plugins.cpog.propertyeditor;

import java.awt.Component;

import javax.swing.JTable;

import org.workcraft.gui.propertyeditor.GenericCellEditor;
import org.workcraft.plugins.cpog.Encoding;

class EncodingCellEditor extends GenericCellEditor
{
    private static final long serialVersionUID = 8L;
    private Encoding encoding;

    @Override
    public Object getCellEditorValue() {
        encoding.updateEncoding((String)super.getCellEditorValue());
        return encoding;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected, int row, int column) {
        encoding = (Encoding)value;
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
