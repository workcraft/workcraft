package org.workcraft.gui.properties;

import org.workcraft.utils.ParseUtils;

public class IntegerProperty extends AbstractBasicProperty<Integer> {

    @Override
    public Integer fromCellEditorValue(String editorComponentValue) {
        return ParseUtils.parseInt(editorComponentValue, 0);
    }

    @Override
    public String toCellRendererValue(Integer value) {
        return (value == null) ? "" : Integer.toString(value);
    }

}
