package org.workcraft.gui.propertyeditor;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public interface PropertyClass {
    TableCellRenderer getCellRenderer();
    TableCellEditor getCellEditor();
    Object fromCellEditorValue(Object editorComponentValue);
    Object toCellRendererValue(Object value);
}
