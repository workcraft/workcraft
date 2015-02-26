package org.workcraft.gui.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class FileCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

	protected static final String EDIT = "edit";
	protected static final String CLEAR = "clear";

	final private JPanel panel;
	final private JButton chooseButton;
	final private JButton clearButton;
	private File file;

	public FileCellEditor() {
		chooseButton = new JButton();
		chooseButton.setActionCommand(EDIT);
		chooseButton.addActionListener(this);
		chooseButton.setOpaque(true);
		chooseButton.setBorderPainted(false);
		chooseButton.setFocusable(false);
		chooseButton.setMargin(new Insets(1, 1, 1, 1));
		chooseButton.setHorizontalAlignment(SwingConstants.LEFT);

		clearButton = new JButton("x");
		clearButton.setActionCommand(CLEAR);
		clearButton.addActionListener(this);
		clearButton.setFocusable(false);
		clearButton.setMargin(new Insets(1, 1, 1, 1));

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(chooseButton, BorderLayout.CENTER);
		panel.add(clearButton, BorderLayout.EAST);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogType(JFileChooser.OPEN_DIALOG);
			fc.setMultiSelectionEnabled(false);
			fc.setSelectedFile(file);
			fc.setDialogTitle("Select file");
			if (fc.showDialog(null, "Open") == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
			}
		} else if (CLEAR.equals(e.getActionCommand())) {
			file = null;
		}
		fireEditingStopped();
	}

	@Override
	public Object getCellEditorValue() {
		return file;
	}

	@Override
	public Component getTableCellEditorComponent(
			JTable table, Object value, boolean isSelected, int row, int column) {
    	file = (File)value;
		return panel;
	}

}
