package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public interface PropertyClass<T, R> {
    TableCellRenderer getCellRenderer();
    TableCellEditor getCellEditor();
    T fromCellEditorValue(R editorComponentValue);
    R toCellRendererValue(T value);
}
