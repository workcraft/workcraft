package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatCellRenderer;
import org.workcraft.utils.ParseUtils;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.IllegalFormatException;

public class DoubleProperty implements PropertyClass<Double, String> {

    @Override
    public TableCellEditor getCellEditor() {
        return new GenericCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new FlatCellRenderer();
    }

    @Override
    public Double fromCellEditorValue(String editorComponentValue) {
        return ParseUtils.parseDouble(editorComponentValue.replace(",", "."), 0.0);
    }

    @Override
    public String toCellRendererValue(Double value) {
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
