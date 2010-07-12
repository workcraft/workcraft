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


import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class ColorCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

	static class Approximator implements ActionListener {
		public ActionListener target = null;

		public void actionPerformed(ActionEvent e) {
			target.actionPerformed(e);
		}
	}

	Color currentColor;
	JButton button;

	static Approximator approx = new Approximator();
	static JColorChooser colorChooser = null;
	static JDialog dialog = null;

	protected static final String EDIT = "edit";

	public  ColorCellEditor() {

		button = new JButton();
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);
		button.setFocusable(false);

//		Set up the dialog that the button brings up.

		if (colorChooser == null)
			colorChooser = new JColorChooser();
		if (dialog == null) {
			dialog = JColorChooser.createDialog(null,
					"Pick a Color",
					true,  //modal
					colorChooser,
					approx,  //OK button handler
					null); //no CANCEL button handler
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
//			The user has clicked the cell, so
//			bring up the dialog.

			approx.target = this;
			button.setBackground(currentColor);
			colorChooser.setColor(currentColor);
			dialog.setVisible(true);

			fireEditingStopped(); //Make the renderer reappear.

		} else { //User pressed dialog's "OK" button.
			currentColor = colorChooser.getColor();
		}
	}

//	Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return currentColor;
	}

//	Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table,
			Object value,
			boolean isSelected,
			int row,
			int column) {
		currentColor = (Color)value;
		return button;
	}
}

