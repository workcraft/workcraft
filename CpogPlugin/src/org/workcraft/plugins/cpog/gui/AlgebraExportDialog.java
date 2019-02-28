package org.workcraft.plugins.cpog.gui;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.workcraft.Framework;
import org.workcraft.utils.GuiUtils;

@SuppressWarnings("serial")
public class AlgebraExportDialog extends JDialog {

    private final JTextField filePath;
    private final JButton selectFileBtn;
    private final JRadioButton pasteRB, exportRB;
    private Boolean okClicked;

    public AlgebraExportDialog() {
        super(Framework.getInstance().getMainWindow(),
                "Export graphs as algebraic expressions", ModalityType.APPLICATION_MODAL);

        filePath = new JTextField(" ", 30);
        filePath.setEditable(true);

        selectFileBtn = GuiUtils.createDialogButton("Select export location");

        selectFileBtn.addActionListener(event -> actionSelectFile());

        JPanel filePanel = new JPanel();
        filePanel.add(filePath);
        filePanel.add(selectFileBtn);

        filePath.setEnabled(false);
        selectFileBtn.setEnabled(false);

        pasteRB = new JRadioButton("Paste expression into algebra text box", false);
        pasteRB.addActionListener(event -> actionPaste());

        exportRB = new JRadioButton("Export expression to file", false);
        exportRB.addActionListener(event -> actionExport());

        JPanel optionPanel = new JPanel();
        optionPanel.add(pasteRB);
        optionPanel.add(exportRB);

        JButton okButton = GuiUtils.createDialogButton("OK");
        okButton.addActionListener(event -> actionOk());

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> actionCancel());

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

    private void actionOk() {
        okClicked = true;
        setVisible(false);
    }

    private void actionCancel() {
        okClicked = false;
        setVisible(false);
    }

    private void actionSelectFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            filePath.setText(f.getAbsolutePath());
        }
    }

    private void actionPaste() {
        if (pasteRB.isSelected()) {
            exportRB.setSelected(false);
            filePath.setEnabled(false);
            selectFileBtn.setEnabled(false);
        }
    }

    private void actionExport() {
        if (exportRB.isSelected()) {
            pasteRB.setSelected(false);
            filePath.setEnabled(true);
            selectFileBtn.setEnabled(true);
        } else {
            filePath.setEnabled(false);
            selectFileBtn.setEnabled(false);
        }
    }

    public Boolean getOK() {
        return okClicked;
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
}
