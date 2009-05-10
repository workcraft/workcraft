package org.workcraft.gui.propertyeditor;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;


@SuppressWarnings("serial")
public class BooleanCellEditor extends AbstractCellEditor implements
		TableCellEditor, ItemListener{

	private JCheckBox checkBox;

	public BooleanCellEditor() {
		 checkBox = new JCheckBox();
		 checkBox.setOpaque(false);
		 checkBox.setFocusable(false);

		 checkBox.addItemListener(this);
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		checkBox.setSelected((Boolean)value);
		return checkBox;
	}

	public Object getCellEditorValue() {
		return checkBox.isSelected();
	}


	public void itemStateChanged(ItemEvent e) {
		fireEditingStopped();
	}

}
