package org.workcraft.gui.properties;

public class StringProperty extends AbstractBasicProperty<String> {

    @Override
    public String fromCellEditorValue(String editorComponentValue) {
        return editorComponentValue;
    }

    @Override
    public String toCellRendererValue(String value) {
        return (value == null) ? "" : value;
    }

}
