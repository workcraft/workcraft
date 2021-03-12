package org.workcraft.gui.properties;

import org.workcraft.utils.ParseUtils;

import java.util.IllegalFormatException;

public class DoubleProperty extends AbstractBasicProperty<Double> {

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
