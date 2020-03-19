package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatCellRenderer;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class StringProperty implements PropertyClass<String, String> {

    @Override
    public TableCellEditor getCellEditor() {
        return new GenericCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new FlatCellRenderer();
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
