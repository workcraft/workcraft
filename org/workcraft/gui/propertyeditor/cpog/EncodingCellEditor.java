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

package org.workcraft.gui.propertyeditor.cpog;


import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.workcraft.plugins.cpog.Encoding;
import org.workcraft.plugins.verification.gui.MpsatPresetManagerDialog;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class EncodingCellEditor extends AbstractCellEditor implements TableCellEditor {

	class Dialog extends JDialog
	{
		public Dialog()
		{
		}

		public Encoding getEncoding()
		{
			return null;
		}

	}
	Encoding currentEncoding;
	JButton button = new JButton();

	Dialog dialog = null;

	class EditListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			dialog.setModalityType(ModalityType.APPLICATION_MODAL);
			//GUI.centerFrameToParent(dialog, this);
			dialog.setVisible(true);
		}
	}

	class DialogOkListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			currentEncoding = dialog.getEncoding();
		}
	}

	class CloseListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			dialog.setVisible(false);
		}
	}

	public  EncodingCellEditor()
	{
		System.out.println("created " + this);
		button.addActionListener(new EditListener());
	}

	private Dialog createDialog(JTable table)
	{
		Dialog dialog = new Dialog();

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new DialogOkListener());
		dialog.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		dialog.add(cancelButton);

		ActionListener closer = new CloseListener();

		okButton.addActionListener(closer);
		cancelButton.addActionListener(closer);

		return dialog;
	}

	public Object getCellEditorValue() {
		return currentEncoding;
	}

	public Component getTableCellEditorComponent(JTable table,
			Object value,
			boolean isSelected,
			int row,
			int column) {
		if(dialog == null)
			dialog = createDialog(table);
		Encoding encoding = (Encoding)value;
		currentEncoding = encoding;
		button.setText(encoding.toString());
		return button;
	}
}

