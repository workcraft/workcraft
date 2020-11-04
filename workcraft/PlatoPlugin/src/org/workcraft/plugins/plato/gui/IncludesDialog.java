package org.workcraft.plugins.plato.gui;

import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;

@SuppressWarnings("serial")
public class IncludesDialog extends JDialog {

    private final JPanel content;
    private final JPanel btnPanel;
    private final File lastDirUsed;
    private static JList<String> includeList;
    private final DefaultListModel<String> includeListModel;

    public IncludesDialog(WriterDialog owner, File lastDirUsed, DefaultListModel<String> includeListModel) {
        super(owner, "Include concept files", ModalityType.APPLICATION_MODAL);

        this.lastDirUsed = lastDirUsed;
        this.includeListModel = includeListModel;

        content = new JPanel();
        content.setLayout(new BorderLayout());

        btnPanel = new JPanel();

        createListPanel();
        createBtnPanel();

        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
        setMinimumSize(new Dimension(500, 500));
        setLocationRelativeTo(owner);
    }

    private void createListPanel() {
        includeList = new JList<>(includeListModel);

        content.add(includeList, BorderLayout.CENTER);
    }

    private void createBtnPanel() {
        JButton addBtn = GuiUtils.createDialogButton("Add");
        JButton removeBtn = GuiUtils.createDialogButton("Remove");
        JButton okBtn = GuiUtils.createDialogButton("OK");

        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(okBtn);

        addBtn.addActionListener(event -> addAction());
        removeBtn.addActionListener(event -> removeAction());
        okBtn.addActionListener(event -> setVisible(false));
    }

    private void addAction() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setFileFilter(new FileNameExtensionFilter("Haskell/Concept file (.hs)", "hs"));
        fc.setCurrentDirectory(lastDirUsed);
        if (DialogUtils.showFileOpener(fc)) {
            File file = fc.getSelectedFile();
            try {
                if (!file.exists()) {
                    throw new FileNotFoundException();
                }
                includeListModel.addElement(file.getAbsolutePath());
            } catch (FileNotFoundException e) {
                DialogUtils.showError(e.getMessage());
            }
        }
    }

    private void removeAction() {
        if (!includeList.isSelectionEmpty()) {
            includeListModel.removeElement(includeList.getSelectedValue());
        }
    }

}
