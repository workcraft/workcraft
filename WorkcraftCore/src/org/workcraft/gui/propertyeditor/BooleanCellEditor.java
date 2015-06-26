/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.propertyeditor;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;


@SuppressWarnings("serial")
public class BooleanCellEditor extends AbstractCellEditor implements TableCellEditor, ItemListener{

	private JCheckBox checkBox;

	public BooleanCellEditor() {
		 checkBox = new JCheckBox();
		 checkBox.setFocusable(false);
		 checkBox.addItemListener(this);
		 checkBox.setBorderPainted(false);
	}

	public Component getTableCellEditorComponent(
			JTable table, Object value,	boolean isSelected, int row, int column) {
		if (value != null) {
			checkBox.setSelected((Boolean)value);
		}
		checkBox.setOpaque(value == null);
		checkBox.setFont(table.getFont());
		return checkBox;
	}

	public Object getCellEditorValue() {
		return checkBox.isSelected();
	}


	public void itemStateChanged(ItemEvent e) {
		fireEditingStopped();
	}

}
