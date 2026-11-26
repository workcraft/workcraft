package org.workcraft.gui.properties;

import org.workcraft.exceptions.ArgumentException;

import java.util.regex.Pattern;

public class IntegerProperty extends AbstractBasicProperty<Integer> {

    private static final Pattern BEGINNING_PATTERN = Pattern.compile("\\s*[+-]?\\d*\\s*");

    @Override
    public boolean isValidBeginning(String text) {
        return (text == null) || BEGINNING_PATTERN.matcher(text).matches();
    }

    @Override
    public Integer fromCellEditorValue(String editorComponentValue) {
        String text = (editorComponentValue == null) ? "" : editorComponentValue.trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            // Rejected input keeps the current value, as the property editor reports the error instead of setting it
            throw new ArgumentException("Value must be an integer");
        }
    }

    @Override
    public String toCellRendererValue(Integer value) {
        return (value == null) ? "" : Integer.toString(value);
    }

}
