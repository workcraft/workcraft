package org.workcraft.gui.properties;

import org.workcraft.exceptions.ArgumentException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class DoubleProperty extends AbstractBasicProperty<Double> {

    private static final Pattern BEGINNING_PATTERN = Pattern.compile("\\s*[+-]?\\d*([.,]\\d*)?([eE][+-]?\\d*)?\\s*");

    @Override
    public boolean isValidBeginning(String text) {
        return (text == null) || BEGINNING_PATTERN.matcher(text).matches();
    }

    @Override
    public Double fromCellEditorValue(String editorComponentValue) {
        String text = (editorComponentValue == null) ? "" : editorComponentValue.trim().replace(",", ".");
        double result;
        try {
            // Unlike Double.parseDouble, BigDecimal accepts plain decimal numbers only (no type suffixes, hex, NaN)
            result = new BigDecimal(text).doubleValue();
        } catch (NumberFormatException e) {
            result = Double.NaN;
        }
        // Rejected input keeps the current value, as the property editor reports the error instead of setting it
        if (!Double.isFinite(result)) {
            throw new ArgumentException("Value must be a number");
        }
        return result;
    }

    @Override
    public String toCellRendererValue(Double value) {
        return (value == null) ? "" : Double.toString(value);
    }

}
