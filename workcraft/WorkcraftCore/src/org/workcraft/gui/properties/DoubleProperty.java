package org.workcraft.gui.properties;

import org.workcraft.utils.ParseUtils;

public class DoubleProperty extends AbstractBasicProperty<Double> {

    @Override
    public Double fromCellEditorValue(String editorComponentValue) {
        return ParseUtils.parseDouble(editorComponentValue.replace(",", "."), 0.0);
    }

    @Override
    public String toCellRendererValue(Double value) {
        return (value == null) ? "" : Double.toString(value);
    }

}
