package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class StringProperty implements PropertyClass<String, String> {

    @Override
    public TableCellEditor getCellEditor() {
        return new GenericCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new DefaultCellRenderer();
    }

    @Override
    public String fromCellEditorValue(String editorComponentValue) {
        return editorComponentValue;
    }

    @Override
    public String toCellRendererValue(String value) {
        return (value == null) ? "" : value;
    }

}
