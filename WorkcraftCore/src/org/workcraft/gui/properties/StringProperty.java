package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class StringProperty implements PropertyClass {

    @Override
    public TableCellEditor getCellEditor() {
        return new GenericCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new DefaultCellRenderer();
    }

    @Override
    public Object fromCellEditorValue(Object editorComponentValue) {
        return editorComponentValue.toString();
    }

    @Override
    public Object toCellRendererValue(Object value) {
        String result = "";
        if (value != null) {
            result = value.toString();
        }
        return result;
    }
}
