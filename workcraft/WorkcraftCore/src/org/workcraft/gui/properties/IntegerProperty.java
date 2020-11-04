package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatCellRenderer;
import org.workcraft.utils.ParseUtils;

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
        return ParseUtils.parseInt(editorComponentValue, 0);
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
