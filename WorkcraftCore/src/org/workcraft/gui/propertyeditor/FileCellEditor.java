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

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class FileCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    protected static final String TAG_EDIT = "edit";
    protected static final String TAG_CLEAR = "clear";

    private final JPanel panel;
    private File file;

    public FileCellEditor() {
        JButton chooseButton = new JButton();
        chooseButton.setActionCommand(TAG_EDIT);
        chooseButton.addActionListener(this);
        chooseButton.setOpaque(true);
        chooseButton.setBorderPainted(false);
        chooseButton.setFocusable(false);
        chooseButton.setMargin(new Insets(1, 1, 1, 1));
        chooseButton.setHorizontalAlignment(SwingConstants.LEFT);

        JButton clearButton = new JButton("x");
        clearButton.setActionCommand(TAG_CLEAR);
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
        if (TAG_EDIT.equals(e.getActionCommand())) {
            final Framework framework = Framework.getInstance();
            JFileChooser fc = new JFileChooser();
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setMultiSelectionEnabled(false);
            boolean fcConfigured = false;
            if (file != null) {
                if (file.exists()) {
                    fc.setSelectedFile(file);
                    fcConfigured = true;
                } else {
                    File dir = file.getParentFile();
                    if ((dir != null) && dir.exists()) {
                        fc.setCurrentDirectory(dir);
                        fcConfigured = true;
                    }
                }
            }
            final MainWindow mainWindow = framework.getMainWindow();
            if (!fcConfigured) {
                WorkspaceEntry we = mainWindow.getCurrentWorkspaceEntry();
                File file = we.getFile();
                File dir = file.exists() ? file.getParentFile() : null;
                if ((dir != null) && dir.exists()) {
                    fc.setCurrentDirectory(dir);
                    fcConfigured = true;
                }
            }
            fc.setDialogTitle("Select file");
            GUI.sizeFileChooserToScreen(fc, mainWindow.getDisplayMode());
            if (fc.showDialog(mainWindow, "Open") == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
            }
        } else if (TAG_CLEAR.equals(e.getActionCommand())) {
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
        file = (File) value;
        panel.setFont(table.getFont());
        return panel;
    }

}
