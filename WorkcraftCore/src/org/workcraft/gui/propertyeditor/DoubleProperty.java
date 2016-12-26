package org.workcraft.gui.propertyeditor;

import java.util.IllegalFormatException;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class DoubleProperty implements PropertyClass {

    public TableCellEditor getCellEditor() {
        return new GenericCellEditor();
    }

    public TableCellRenderer getCellRenderer() {
        return new DefaultCellRenderer();
    }

    public Object fromCellEditorValue(Object editorComponentValue) {
        try {
            String s = (String) editorComponentValue;
            return Double.parseDouble(s.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public Object toCellRendererValue(Object value) {
        String result = "";
        if (value != null) {
            try {
                result = String.format("%.2f", value);
            } catch (IllegalFormatException e) {
                result = "#getter did not return a double";
            }
        }
        return result;
    }
}
