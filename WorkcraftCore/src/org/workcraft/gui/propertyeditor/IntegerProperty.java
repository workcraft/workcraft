package org.workcraft.gui.propertyeditor;

import java.util.IllegalFormatException;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class IntegerProperty implements PropertyClass {

    public TableCellEditor getCellEditor() {
        return new GenericCellEditor();
    }

    public TableCellRenderer getCellRenderer() {
        return new DefaultCellRenderer();
    }

    public Object fromCellEditorValue(Object editorComponentValue) {
        try {
            return Integer.parseInt((String) editorComponentValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public Object toCellRendererValue(Object value) {
        String result = "";
        if (value != null) {
            try {
                result = String.format("%d", value);
            } catch (IllegalFormatException e) {
                result = "#getter did not return an int";
            }
        }
        return result;
    }

}
