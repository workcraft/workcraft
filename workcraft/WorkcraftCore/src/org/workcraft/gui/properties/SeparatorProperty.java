package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class SeparatorProperty implements PropertyClass<Separator, String> {

    @Override
    public TableCellRenderer getCellRenderer() {
        return new SeparatorCellRenderer();
    }

    @Override
    public TableCellEditor getCellEditor() {
        return null;
    }

    @Override
    public Separator fromCellEditorValue(String editorComponentValue) {
        return new Separator(editorComponentValue);
    }

    @Override
    public String toCellRendererValue(Separator value) {
        return (value == null) || (value.text() == null) ? "" : value.text();
    }

}
