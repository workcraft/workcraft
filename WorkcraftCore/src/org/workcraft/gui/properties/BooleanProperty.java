package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class BooleanProperty implements PropertyClass<Boolean, Boolean> {

    @Override
    public TableCellEditor getCellEditor() {
        return new BooleanCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new BooleanCellRenderer();
    }

    @Override
    public Boolean fromCellEditorValue(Boolean editorComponentValue) {
        return editorComponentValue;
    }

    @Override
    public Boolean toCellRendererValue(Boolean value) {
        return value;
    }
}
