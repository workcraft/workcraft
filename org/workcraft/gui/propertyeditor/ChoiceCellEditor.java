package org.workcraft.gui.propertyeditor;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class ChoiceCellEditor extends AbstractCellEditor implements TableCellEditor, ItemListener {
	private JComboBox comboBox;
	private ChoiceWrapper[] wrappers;


	public ChoiceCellEditor(PropertyDescriptor decl) {
		comboBox = new JComboBox();
		comboBox.setEditable(false);
		comboBox.setFocusable(false);
		comboBox.addItemListener(this);

		wrappers = new ChoiceWrapper[decl.getChoice().size()];
		int j = 0;
		for (Object o : decl.getChoice().keySet()) {
			wrappers[j] = new ChoiceWrapper(decl.getChoice().get(o), o);
			comboBox.addItem(wrappers[j]);
			j++;
		}

	}

	public Object getCellEditorValue() {
		return ((ChoiceWrapper)comboBox.getSelectedItem()).value;
	}

	public Component getTableCellEditorComponent(JTable table,
			Object value,
			boolean isSelected,
			int row,
			int column) {
		for (ChoiceWrapper w : wrappers)
			if (w.text.equals(value))
				comboBox.setSelectedItem(w);
		return comboBox;
	}

	public void itemStateChanged(ItemEvent e) {
		fireEditingStopped();
	}
}

