package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatCellRenderer;

import java.util.IllegalFormatException;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

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
        try {
            String s = editorComponentValue;
            return Double.parseDouble(s.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
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
