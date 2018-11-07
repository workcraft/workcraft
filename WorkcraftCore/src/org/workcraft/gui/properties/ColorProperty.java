package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ColorProperty implements PropertyClass {

    @Override
    public TableCellEditor getCellEditor() {
        return new ColorCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new ColorCellRenderer();
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
