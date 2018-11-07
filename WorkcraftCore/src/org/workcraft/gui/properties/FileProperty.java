package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class FileProperty implements PropertyClass {

    @Override
    public TableCellEditor getCellEditor() {
        return new FileCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new FileCellRenderer();
    }

    @Override
    public Object fromCellEditorValue(Object value) {
        return value;
    }

    @Override
    public Object toCellRendererValue(Object value) {
        return value;
    }
}
