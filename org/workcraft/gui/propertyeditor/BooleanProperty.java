package org.workcraft.gui.propertyeditor;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class BooleanProperty implements PropertyClass {

	public Object fromCellEditorValue(Object editorComponentValue) {
		return editorComponentValue;
	}

	public TableCellEditor getCellEditor() {
		return new BooleanCellEditor();
	}

	public TableCellRenderer getCellRenderer() {
		return new BooleanCellRenderer();
	}

	public Object toCellRendererValue(Object value) {
		return value;
	}
}
