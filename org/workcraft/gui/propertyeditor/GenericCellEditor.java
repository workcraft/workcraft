package org.workcraft.gui.propertyeditor;

import java.awt.Component;
import java.awt.event.ItemEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class GenericCellEditor extends AbstractCellEditor implements TableCellEditor {
	private JTextField textField;

	public GenericCellEditor() {
		textField = new JTextField();
		textField.setFocusable(true);
	}

	public Object getCellEditorValue() {
		return textField.getText();
	}

	public Component getTableCellEditorComponent(JTable table,
			Object value,
			boolean isSelected,
			int row,
			int column) {
		textField.setText(value.toString());
		return textField;
	}

	public void itemStateChanged(ItemEvent e) {
		fireEditingStopped();
	}
}

