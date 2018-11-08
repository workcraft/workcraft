package org.workcraft.plugins.cpog.properties;

import org.workcraft.gui.properties.PropertyClass;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class EncodingProperty implements PropertyClass {

    public Object fromCellEditorValue(Object editorComponentValue) {
        return editorComponentValue;
    }

    public TableCellEditor getCellEditor() {
        return new EncodingCellEditor();
    }

    public TableCellRenderer getCellRenderer() {
        return new DefaultTableCellRenderer();
    }

    public Object toCellRendererValue(Object value) {
        return value;
    }

}
