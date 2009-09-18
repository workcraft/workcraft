package org.workcraft.gui.propertyeditor;

import java.util.IllegalFormatException;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class DoubleProperty implements PropertyClass {

	public TableCellEditor getCellEditor() {
		GenericCellEditor dce = new GenericCellEditor();
		return dce;
	}

	public TableCellRenderer getCellRenderer() {
		return new DefaultTableCellRenderer();
	}

	public Object fromCellEditorValue(Object editorComponentValue) {
		try {
			return Double.parseDouble((String)editorComponentValue);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	public Object toCellRendererValue(Object value) {
		try {
			return String.format("%.2f", value);
		} catch (IllegalFormatException e) {
			return "#getter did not return a double";
		}
	}
}
