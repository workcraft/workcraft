package org.workcraft.gui.propertyeditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class FileCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

	protected static final String EDIT = "edit";

	JButton button;
	File file;

	public  FileCellEditor() {
		button = new JButton();
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);
		button.setFocusable(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogType(JFileChooser.OPEN_DIALOG);
			fc.setMultiSelectionEnabled(false);
			fc.setSelectedFile(file);
			fc.setDialogTitle("Open environment file");
			if (fc.showDialog(null, "Open") == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				fireEditingStopped(); //Make the renderer reappear.
			}
		}
	}

	@Override
	public Object getCellEditorValue() {
		return file;
	}

	@Override
	public Component getTableCellEditorComponent(
			JTable table, Object value, boolean isSelected, int row, int column) {
		file = (File)value;
		return button;
	}

}
