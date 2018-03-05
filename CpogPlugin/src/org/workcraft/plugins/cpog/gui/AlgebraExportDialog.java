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
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class AlgebraExportDialog extends JDialog {

    private final JTextField filePath;
    private final JButton selectFileBtn, okButton, cancelButton;
    private final JRadioButton pasteRB, exportRB;
    private Boolean okClicked;

    public AlgebraExportDialog() {
        super(Framework.getInstance().getMainWindow(),
                "Export graphs as algebraic expressions", ModalityType.APPLICATION_MODAL);

        filePath = new JTextField(" ", 30);
        filePath.setEditable(true);

        selectFileBtn = GUI.createDialogButton("Select export location");

        addSelectFileBtnListener();

        JPanel filePanel = new JPanel();
        filePanel.add(filePath);
        filePanel.add(selectFileBtn);

        filePath.setEnabled(false);
        selectFileBtn.setEnabled(false);

        pasteRB = new JRadioButton("Paste expression into algebra text box", false);
        exportRB = new JRadioButton("Export expression to file", false);
        addRadioButtonListener();

        JPanel optionPanel = new JPanel();
        optionPanel.add(pasteRB);
        optionPanel.add(exportRB);

        okButton = GUI.createDialogButton("OK");
        cancelButton = GUI.createDialogButton("Cancel");

        JPanel okPanel = new JPanel();
        okPanel.add(okButton);
        okPanel.add(cancelButton);
        addButtonListeners();

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

    public void addSelectFileBtnListener() {
        selectFileBtn.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                filePath.setText(f.getAbsolutePath());
            }
        });
    }

    public void addRadioButtonListener() {
        pasteRB.addActionListener(event -> {
            if (pasteRB.isSelected()) {
                exportRB.setSelected(false);
                filePath.setEnabled(false);
                selectFileBtn.setEnabled(false);
            }
        });

        exportRB.addActionListener(event -> {
            if (exportRB.isSelected()) {
                pasteRB.setSelected(false);
                filePath.setEnabled(true);
                selectFileBtn.setEnabled(true);
            } else {
                filePath.setEnabled(false);
                selectFileBtn.setEnabled(false);
            }

        });
    }

    public void addButtonListeners() {
        okButton.addActionListener(event -> {
            okClicked = true;
            setVisible(false);
        });

        cancelButton.addActionListener(event -> {
            okClicked = false;
            setVisible(false);
        });
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
