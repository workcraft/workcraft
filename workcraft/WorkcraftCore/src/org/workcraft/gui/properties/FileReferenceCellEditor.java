package org.workcraft.gui.properties;

import org.workcraft.Framework;
import org.workcraft.dom.references.FileReference;
import org.workcraft.gui.MainWindow;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class FileReferenceCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private static final String ENTER_TAG = "enter";
    private static final String CHOOSE_TAG = "choose";
    private static final String CLEAR_TAG = "clear";

    private final JPanel panel;
    private final JButton enterButton;
    private final JButton chooseButton;
    private final JButton clearButton;

    private FileReference fileReference;

    public FileReferenceCellEditor() {
        enterButton = new JButton(PropertyHelper.ENTER_SYMBOL);
        enterButton.setActionCommand(ENTER_TAG);
        enterButton.addActionListener(this);
        enterButton.setEnabled(false);
        enterButton.setFocusable(false);
        enterButton.setMargin(PropertyHelper.BUTTON_INSETS);

        chooseButton = new JButton();
        chooseButton.setActionCommand(CHOOSE_TAG);
        chooseButton.addActionListener(this);
        chooseButton.setOpaque(true);
        chooseButton.setBorderPainted(false);
        chooseButton.setFocusable(false);
        chooseButton.setMargin(PropertyHelper.BUTTON_INSETS);
        chooseButton.setHorizontalAlignment(SwingConstants.LEFT);

        clearButton = new JButton(PropertyHelper.CLEAR_SYMBOL);
        clearButton.setActionCommand(CLEAR_TAG);
        clearButton.addActionListener(this);
        clearButton.setEnabled(false);
        clearButton.setFocusable(false);
        clearButton.setMargin(PropertyHelper.BUTTON_INSETS);

        panel = new JPanel(new BorderLayout());
        panel.add(enterButton, BorderLayout.WEST);
        panel.add(chooseButton, BorderLayout.CENTER);
        panel.add(clearButton, BorderLayout.EAST);
        panel.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case ENTER_TAG:
            enterAction();
            break;
        case CHOOSE_TAG:
            chooseAction();
            break;
        case CLEAR_TAG:
            clearAction();
            break;
        }
        fireEditingStopped();
    }

    private void enterAction() {
        if (fileReference != null) {
            File file = fileReference.getFile();
            if (file != null) {
                MainWindow mainWindow = Framework.getInstance().getMainWindow();
                WorkspaceEntry we = mainWindow.openWork(file);
                mainWindow.requestFocus(we);
            }
        }
    }

    private void chooseAction() {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        JFileChooser fc = mainWindow.createOpenDialog("Select work file", false, true, null);
        File file = null;
        if (fileReference != null) {
            file = fileReference.getFile();
        }
        if (file != null) {
            File dir = file.getParentFile();
            if ((dir != null) && dir.exists()) {
                fc.setCurrentDirectory(dir);
            }
            if (file.exists()) {
                fc.setSelectedFile(file);
            }
        }
        if (fc.showDialog(mainWindow, "Open") == JFileChooser.APPROVE_OPTION) {
            if (fileReference == null) {
                fileReference = new FileReference();
            }
            try {
                fileReference.setPath(fc.getSelectedFile().getCanonicalPath());
            } catch (IOException e) {
            }
            update();
        }
    }

    private void clearAction() {
        fileReference = null;
        update();
    }

    @Override
    public FileReference getCellEditorValue() {
        return fileReference;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {

        if (value instanceof FileReference) {
            fileReference = (FileReference) value;
            update();
        }
        if (table != null) {
            Font font = table.getFont();
            enterButton.setFont(font);
            chooseButton.setFont(font);
            clearButton.setFont(font);
            panel.setFont(font);
        }
        return panel;
    }

    private void update() {
        enterButton.setEnabled(false);
        clearButton.setEnabled(false);
        if (fileReference != null) {
            File file = fileReference.getFile();
            chooseButton.setText(file.getName());
            if (file != null) {
                if (file.exists()) {
                    enterButton.setEnabled(true);
                    chooseButton.setForeground(Color.BLACK);
                } else {
                    chooseButton.setForeground(Color.RED);
                }
                clearButton.setEnabled(true);
            }
        }
    }

}
