package org.workcraft.gui.propertyeditor;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class BooleanCellRenderer extends JCheckBox implements TableCellRenderer  {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setSelected ((Boolean)value);
		setOpaque(false);
		setFocusable(false);
		return this;
	}

}
