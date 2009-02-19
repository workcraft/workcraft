package org.workcraft.gui.propertyeditor;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class StringProperty implements PropertyClass {

	public TableCellEditor getCellEditor() {
		GenericCellEditor dce = new GenericCellEditor();
		return dce;
	}

	public TableCellRenderer getCellRenderer() {
		return new DefaultTableCellRenderer();
	}

	public Object fromCellEditorValue(Object editorComponentValue) {
		return editorComponentValue.toString();
	}

	public Object toCellRendererValue(Object value) {
		return value.toString();
	}
}
