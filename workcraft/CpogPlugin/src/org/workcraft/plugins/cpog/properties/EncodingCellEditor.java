package org.workcraft.plugins.cpog.properties;

import org.workcraft.gui.properties.BasicCellEditor;
import org.workcraft.plugins.cpog.Encoding;

import javax.swing.*;
import java.awt.*;

class EncodingCellEditor extends BasicCellEditor {

    private static final long serialVersionUID = 8L;
    private Encoding encoding;

    @Override
    public Encoding getCellEditorValue() {
        encoding.updateEncoding((String) super.getCellEditorValue());
        return encoding;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof Encoding) {
            encoding = (Encoding) value;
        }
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

}
