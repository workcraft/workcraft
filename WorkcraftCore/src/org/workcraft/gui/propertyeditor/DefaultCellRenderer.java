package org.workcraft.gui.propertyeditor;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class DefaultCellRenderer extends JLabel implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value != null) {
			setText((String)value);
		} else {
			setText("");
		}
		setFont(table.getFont());
		setOpaque(value == null || value.equals(""));
		return this;
	}

}
