package org.workcraft.plugins.cpog.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.workcraft.Framework;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class PGMinerImportDialog extends JDialog {

    private final JTextField filePath;
    private final JButton    selectFileBtn, importButton, cancelButton;
    private final JCheckBox extractConcurrencyCB, splitCB;
    private boolean canImport;

    public PGMinerImportDialog() {
        super(Framework.getInstance().getMainWindow(), "Import event log", ModalityType.APPLICATION_MODAL);

        canImport = false;

        filePath = new JTextField("", 25);
        filePath.setEditable(true);

        selectFileBtn = GUI.createDialogButton("Browse for file");

        addSelectFileBtnListener();

        JPanel filePanel = new JPanel();
        JPanel selPanel = new JPanel(new FlowLayout());
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.PAGE_AXIS));
        filePanel.add(filePath);
        selPanel.add(selectFileBtn);
        filePanel.add(selPanel);

        extractConcurrencyCB = new JCheckBox("Perform concurrency extraction", false);
        splitCB = new JCheckBox("Split traces into scenarios", false);

        splitCB.setEnabled(false);
        addCheckBoxListener();

        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.PAGE_AXIS));
        optionPanel.add(extractConcurrencyCB);
        optionPanel.add(splitCB);

        importButton = GUI.createDialogButton("Import");
        cancelButton = GUI.createDialogButton("Cancel");

        addButtonListeners();

        JPanel btnPanel = new JPanel();
        btnPanel.add(importButton);
        btnPanel.add(cancelButton);

        JPanel content = new JPanel(new BorderLayout());
        content.add(filePanel, BorderLayout.NORTH);
        content.add(optionPanel);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);

        pack();
        this.setLocationRelativeTo(Framework.getInstance().getMainWindow());
    }

    public boolean getExtractConcurrency() {
        return extractConcurrencyCB.isSelected();
    }

    public boolean getSplit() {
        return splitCB.isSelected();
    }

    public String getFilePath() {
        return filePath.getText();
    }

    public void addCheckBoxListener() {
        extractConcurrencyCB.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (extractConcurrencyCB.isSelected()) {
                    splitCB.setEnabled(true);
                } else {
                    splitCB.setSelected(false);
                    splitCB.setEnabled(false);
                }

            }

        });
    }

    public void addSelectFileBtnListener() {
        selectFileBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    try {
                        if (!f.exists()) {
                            throw new FileNotFoundException();
                        }

                        filePath.setText(f.getAbsolutePath());

                    } catch (FileNotFoundException e1) {
                        // TODO Auto-generated catch block
                        JOptionPane.showMessageDialog(null, e1.getMessage(),
                                "File not found error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        });
    }

    public void addButtonListeners() {
        importButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                File eventLog = new File(filePath.getText());
                if (!eventLog.exists()) {
                    JOptionPane.showMessageDialog(null, "The event log chosen does not exist",
                            "File not found", JOptionPane.ERROR_MESSAGE);
                } else {
                    canImport = true;
                    setVisible(false);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                canImport = false;
                setVisible(false);
            }

        });
    }

    public boolean getCanImport() {
        return canImport;
    }

}
