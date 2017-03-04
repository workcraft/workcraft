package org.workcraft.plugins.stg.concepts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class ConceptsIncludesDialog extends JDialog {

    private final JPanel content, btnPanel;
    private final File lastDirUsed;
    private static JList<String> includeList;
    private final DefaultListModel<String> includeListModel;

    public ConceptsIncludesDialog(ConceptsWriterDialog parent, File lastDirUsed,
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

        addBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
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
                        JOptionPane.showMessageDialog(null, e1.getMessage(), "File not found error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
        });

        removeBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!includeList.isSelectionEmpty()) {
                    includeListModel.removeElement(includeList.getSelectedValue());
                }
            }

        });

        okBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }

        });
    }

}
