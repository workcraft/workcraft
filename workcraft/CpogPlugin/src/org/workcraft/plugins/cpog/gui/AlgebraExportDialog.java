package org.workcraft.plugins.cpog.gui;

import org.workcraft.Framework;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class AlgebraExportDialog extends JDialog {

    private final JTextField filePath;
    private final JButton selectFileBtn;
    private final JRadioButton pasteRB;
    private final JRadioButton exportRB;
    private boolean modalResult;

    public AlgebraExportDialog() {
        super(Framework.getInstance().getMainWindow(),
                "Export graphs as algebraic expressions", ModalityType.APPLICATION_MODAL);

        filePath = new JTextField(" ", 30);
        filePath.setEditable(true);

        selectFileBtn = GuiUtils.createDialogButton("Select export location");

        selectFileBtn.addActionListener(event -> selectFileAction());

        JPanel filePanel = new JPanel();
        filePanel.add(filePath);
        filePanel.add(selectFileBtn);

        filePath.setEnabled(false);
        selectFileBtn.setEnabled(false);

        pasteRB = new JRadioButton("Paste expression into algebra text box", false);
        pasteRB.addActionListener(event -> pasteAction());

        exportRB = new JRadioButton("Export expression to file", false);
        exportRB.addActionListener(event -> exportAction());

        JPanel optionPanel = new JPanel();
        optionPanel.add(pasteRB);
        optionPanel.add(exportRB);

        JButton okButton = GuiUtils.createDialogButton("OK");
        okButton.addActionListener(event -> okAction());

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> cancelAction());

        JPanel okPanel = new JPanel();
        okPanel.add(okButton);
        okPanel.add(cancelButton);

        setLayout(new GridLayout(3, 0));
        add(optionPanel);
        add(filePanel);
        add(okPanel);

        getRootPane().registerKeyboardAction(event -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(Framework.getInstance().getMainWindow());
    }

    private void okAction() {
        modalResult = true;
        setVisible(false);
    }

    private void cancelAction() {
        modalResult = false;
        setVisible(false);
    }

    private void selectFileAction() {
        JFileChooser fc = new JFileChooser();
        if (DialogUtils.showFileSaver(fc)) {
            File file = fc.getSelectedFile();
            filePath.setText(file.getAbsolutePath());
        }
    }

    private void pasteAction() {
        if (pasteRB.isSelected()) {
            exportRB.setSelected(false);
            filePath.setEnabled(false);
            selectFileBtn.setEnabled(false);
        }
    }

    private void exportAction() {
        if (exportRB.isSelected()) {
            pasteRB.setSelected(false);
            filePath.setEnabled(true);
            selectFileBtn.setEnabled(true);
        } else {
            filePath.setEnabled(false);
            selectFileBtn.setEnabled(false);
        }
    }

    public Boolean getPaste() {
        return pasteRB.isSelected();
    }

    public Boolean getExport() {
        return exportRB.isSelected();
    }

    public String getFilePath() {
        return filePath.getText();
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}
