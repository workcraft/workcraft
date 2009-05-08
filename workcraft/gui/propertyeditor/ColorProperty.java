package org.workcraft.gui.propertyeditor;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ColorProperty implements PropertyClass {

	public Object fromCellEditorValue(Object editorComponentValue) {
		return editorComponentValue;
	}

	public TableCellEditor getCellEditor() {
		return new ColorCellEditor();
	}

	public TableCellRenderer getCellRenderer() {
		return new ColorCellRenderer(true);
	}

	public Object toCellRendererValue(Object value) {
		return value;
	}
}
