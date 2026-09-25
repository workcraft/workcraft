package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ToggleProperty implements PropertyClass<Toggle, Toggle> {

    @Override
    public TableCellEditor getCellEditor() {
        return new ToggleCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new ToggleCellRenderer();
    }

    @Override
    public Toggle fromCellEditorValue(Toggle value) {
        return value;
    }

    @Override
    public Toggle toCellRendererValue(Toggle value) {
        return value;
    }

}
