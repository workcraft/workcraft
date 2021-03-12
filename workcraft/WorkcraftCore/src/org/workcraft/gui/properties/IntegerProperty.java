package org.workcraft.gui.properties;

import org.workcraft.utils.ParseUtils;

import java.util.IllegalFormatException;

public class IntegerProperty extends AbstractBasicProperty<Integer> {

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
