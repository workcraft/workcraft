package org.workcraft.plugins.plato.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.workcraft.util.GUI;
import org.workcraft.util.DialogUtils;

@SuppressWarnings("serial")
public class PlatoIncludesDialog extends JDialog {

    private final JPanel content, btnPanel;
    private final File lastDirUsed;
    private static JList<String> includeList;
    private final DefaultListModel<String> includeListModel;

    public PlatoIncludesDialog(PlatoWriterDialog parent, File lastDirUsed,
            DefaultListModel<String> includeListModel) {
        super(parent, "Include concept files", ModalityType.APPLICATION_MODAL);

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
        this.setLocationRelativeTo(parent);
    }

    private void createListPanel() {
        includeList = new JList<String>(includeListModel);

        content.add(includeList, BorderLayout.CENTER);
    }

    private void createBtnPanel() {
        JButton addBtn = GUI.createDialogButton("Add");
        JButton removeBtn = GUI.createDialogButton("Remove");
        JButton okBtn = GUI.createDialogButton("OK");

        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(okBtn);

        addBtn.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter(new FileNameExtensionFilter("Haskell/Concept file (.hs)", "hs"));
            chooser.setCurrentDirectory(lastDirUsed);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                try {
                    if (!f.exists()) {
                        throw new FileNotFoundException();
                    }
                    includeListModel.addElement(f.getAbsolutePath());
                    // includeList.setListData(includeListModel);
                } catch (FileNotFoundException e1) {
                    DialogUtils.showError(e1.getMessage());
                }

            }
        });

        removeBtn.addActionListener(event -> {
            if (!includeList.isSelectionEmpty()) {
                includeListModel.removeElement(includeList.getSelectedValue());
            }
        });

        okBtn.addActionListener(event -> setVisible(false));
    }

}
