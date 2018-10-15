package org.workcraft.plugins.cpog.properties;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.workcraft.gui.propertyeditor.PropertyClass;
import org.workcraft.plugins.cpog.Encoding;

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
        return (Encoding) value;
    }
}
