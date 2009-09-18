package org.workcraft.gui.propertyeditor;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public interface PropertyClass {
	public TableCellRenderer getCellRenderer();
	public TableCellEditor getCellEditor();
	public Object fromCellEditorValue(Object editorComponentValue);
	public Object toCellRendererValue(Object value);
}
