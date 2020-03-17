package org.workcraft.plugins.cpog.gui;

import org.workcraft.Framework;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;

@SuppressWarnings("serial")
public class PGMinerImportDialog extends JDialog {

    private final JTextField filePath;
    private final JCheckBox extractConcurrencyCB;
    private final JCheckBox splitCB;
    private boolean modalResult;

    public PGMinerImportDialog() {
        super(Framework.getInstance().getMainWindow(), "Import event log", ModalityType.APPLICATION_MODAL);

        filePath = new JTextField("", 25);
        filePath.setEditable(true);

        JButton selectFileBtn = GuiUtils.createDialogButton("Browse for file");

        selectFileBtn.addActionListener(event -> selectAction());

        JPanel filePanel = new JPanel();
        JPanel selPanel = new JPanel(new FlowLayout());
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.PAGE_AXIS));
        filePanel.add(filePath);
        selPanel.add(selectFileBtn);
        filePanel.add(selPanel);

        extractConcurrencyCB = new JCheckBox("Perform concurrency extraction", false);
        splitCB = new JCheckBox("Split traces into scenarios", false);

        splitCB.setEnabled(false);
        extractConcurrencyCB.addActionListener(event -> extractAction());

        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.PAGE_AXIS));
        optionPanel.add(extractConcurrencyCB);
        optionPanel.add(splitCB);

        JButton importButton = GuiUtils.createDialogButton("Import");
        importButton.addActionListener(event -> importAction());

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> cancelAction());

        JPanel btnPanel = new JPanel();
        btnPanel.add(importButton);
        btnPanel.add(cancelButton);

        JPanel content = new JPanel(new BorderLayout());
        content.add(filePanel, BorderLayout.NORTH);
        content.add(optionPanel);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);

        getRootPane().registerKeyboardAction(event -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(Framework.getInstance().getMainWindow());
    }

    private void selectAction() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                if (!file.exists()) {
                    throw new FileNotFoundException();
                }
                filePath.setText(file.getAbsolutePath());
            } catch (FileNotFoundException e1) {
                DialogUtils.showError(e1.getMessage());
            }
        }
    }

    private void extractAction() {
        if (extractConcurrencyCB.isSelected()) {
            splitCB.setEnabled(true);
        } else {
            splitCB.setSelected(false);
            splitCB.setEnabled(false);
        }
    }

    private void importAction() {
        File eventLog = new File(filePath.getText());
        if (!eventLog.exists()) {
            DialogUtils.showError("The event log chosen does not exist");
        } else {
            modalResult = true;
            setVisible(false);
        }
    }

    private void cancelAction() {
        modalResult = false;
        setVisible(false);
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

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}
