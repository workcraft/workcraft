package org.workcraft.gui.propertyeditor;

import java.util.IllegalFormatException;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class IntegerProperty implements PropertyClass {

	public TableCellEditor getCellEditor() {
		GenericCellEditor dce = new GenericCellEditor();
		return dce;
	}

	public TableCellRenderer getCellRenderer() {
		return new DefaultTableCellRenderer();
	}

	public Object fromCellEditorValue(Object editorComponentValue) {
		try {
			return Integer.parseInt((String)editorComponentValue);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	public Object toCellRendererValue(Object value) {
		try {
			return String.format("%d", value);
		} catch (IllegalFormatException e) {
			return "#getter did not return an int";
		}
	}

}
