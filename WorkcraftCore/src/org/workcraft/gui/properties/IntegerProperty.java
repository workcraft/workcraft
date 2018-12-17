package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.IllegalFormatException;

public class IntegerProperty implements PropertyClass<Integer, String> {

    @Override
    public TableCellEditor getCellEditor() {
        return new GenericCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new FlatCellRenderer();
    }

    @Override
    public Integer fromCellEditorValue(String editorComponentValue) {
        try {
            return Integer.parseInt(editorComponentValue);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public String toCellRendererValue(Integer value) {
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
