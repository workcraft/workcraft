package org.workcraft.gui.properties;

import org.workcraft.utils.DialogUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FileCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private static final String TAG_CHOOSE = "choose";
    private static final String TAG_CLEAR = "clear";

    private final JPanel panel;
    private final JButton clearButton;

    private File file;

    public FileCellEditor() {
        JButton chooseButton = new JButton();
        chooseButton.setActionCommand(TAG_CHOOSE);
        chooseButton.addActionListener(this);
        chooseButton.setOpaque(true);
        chooseButton.setBorderPainted(false);
        chooseButton.setFocusable(false);
        chooseButton.setMargin(PropertyHelper.BUTTON_INSETS);
        chooseButton.setHorizontalAlignment(SwingConstants.LEFT);

        clearButton = new JButton(PropertyHelper.CLEAR_SYMBOL);
        clearButton.setActionCommand(TAG_CLEAR);
        clearButton.addActionListener(this);
        clearButton.setFocusable(false);
        clearButton.setMargin(PropertyHelper.BUTTON_INSETS);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chooseButton, BorderLayout.CENTER);
        panel.add(clearButton, BorderLayout.EAST);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (TAG_CHOOSE.equals(e.getActionCommand())) {
            chooseFile();
        } else if (TAG_CLEAR.equals(e.getActionCommand())) {
            file = null;
        }
        fireEditingStopped();
    }

    private void chooseFile() {
        JFileChooser fc = DialogUtils.createFileOpener("Select file", false, null);

        if (file != null) {
            File dir = file.getParentFile();
            if ((dir != null) && dir.exists()) {
                fc.setCurrentDirectory(dir);
            }
            if (file.exists()) {
                fc.setSelectedFile(file);
            }
        }

        if (DialogUtils.showFileOpener(fc)) {
            file = fc.getSelectedFile();
        }
    }

    @Override
    public File getCellEditorValue() {
        return file;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {

        if (value instanceof File) {
            file = (File) value;
        }
        if (table != null) {
            int buttonSize = table.getRowHeight(row);
            Dimension buttonDimension = new Dimension(buttonSize, buttonSize);
            clearButton.setPreferredSize(buttonDimension);

            panel.setFont(table.getFont());
        }
        return panel;
    }

}
