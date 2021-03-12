package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.Map;

public interface PropertyClass<T, R> {
    TableCellRenderer getCellRenderer();
    TableCellEditor getCellEditor();
    T fromCellEditorValue(R editorComponentValue);
    R toCellRendererValue(T value);

    default TableCellRenderer getCellRenderer(boolean hasPredefinedValues) {
        return getCellRenderer();
    }

    default TableCellEditor getCellEditor(Map<T, R> predefinedValues) {
        return getCellEditor();
    }

}
