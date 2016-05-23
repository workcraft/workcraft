package org.workcraft.plugins.cpog.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.workcraft.Framework;

public class AlgebraExportDialog extends JDialog {

    private final JTextField filePath;
    private final JButton selectFileBtn, okButton, cancelButton;
    private final JRadioButton pasteRB, exportRB;
    private final JPanel optionPanel, okPanel, filePanel;
    private Boolean okClicked;

    public AlgebraExportDialog() {
        super(Framework.getInstance().getMainWindow(), "Export graphs as algebraic expressions", ModalityType.APPLICATION_MODAL);

        filePath = new JTextField(" ", 30);
        filePath.setEditable(true);

        selectFileBtn = new JButton("Select export location");

        addSelectFileBtnListener();

        filePanel = new JPanel();
        filePanel.add(filePath);
        filePanel.add(selectFileBtn);

        filePath.setEnabled(false);
        selectFileBtn.setEnabled(false);

        pasteRB = new JRadioButton("Paste expression into algebra text box", false);
        exportRB = new JRadioButton("Export expression to file", false);
        addRadioButtonListener();

        optionPanel = new JPanel();
        optionPanel.add(pasteRB);
        optionPanel.add(exportRB);

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        okButton.setPreferredSize(new Dimension(100, 25));
        cancelButton.setPreferredSize(new Dimension(100, 25));

        okPanel = new JPanel();
        okPanel.add(okButton);
        okPanel.add(cancelButton);
        addButtonListeners();

        setLayout(new GridLayout(3, 0));
        add(optionPanel);
        add(filePanel);
        add(okPanel);

        this.setSize(400, 220);
        this.setLocationRelativeTo(Framework.getInstance().getMainWindow());
    }

    public void addSelectFileBtnListener() {
        selectFileBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    filePath.setText(f.getAbsolutePath());
                }
            }

        });
    }

    public void addRadioButtonListener() {
        pasteRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (pasteRB.isSelected()) {
                    exportRB.setSelected(false);
                    filePath.setEnabled(false);
                    selectFileBtn.setEnabled(false);
                }
            }

        });

        exportRB.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (exportRB.isSelected()) {
                    pasteRB.setSelected(false);
                    filePath.setEnabled(true);
                    selectFileBtn.setEnabled(true);
                } else {
                    filePath.setEnabled(false);
                    selectFileBtn.setEnabled(false);
                }

            }

        });
    }

    public void addButtonListeners() {
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                okClicked = true;
                setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                okClicked = false;
                setVisible(false);
            }

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
