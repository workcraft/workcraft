package org.workcraft.gui.propertyeditor;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public interface PropertyClass {
	public TableCellRenderer getCellRenderer();
	public TableCellEditor getCellEditor();
	public Object fromComponentValue(Object editorComponentValue);
	public Object toComponentValue(Object value);
}
