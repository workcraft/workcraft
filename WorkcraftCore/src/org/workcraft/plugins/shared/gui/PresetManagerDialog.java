package org.workcraft.plugins.shared.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.workcraft.plugins.shared.presets.Preset;
import org.workcraft.plugins.shared.presets.PresetManager;

@SuppressWarnings("serial")
public class PresetManagerDialog<T> extends JDialog {
    private JPanel content;
    private JList list;
    private JButton okButton, deleteButton, renameButton;
    private JScrollPane listScroll;
    private DefaultListModel listDataModel;

    public PresetManagerDialog(Window owner, final PresetManager<T> presetManager) {
        super(owner, "Manage presets");

        double[][] size = {
                {TableLayout.FILL, 100 },
                {20, 20, TableLayout.FILL, 20 },
        };

        TableLayout layout = new TableLayout(size);
        layout.setVGap(4);
        layout.setHGap(4);

        content = new JPanel(layout);
        content.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        this.setLayout(new BorderLayout());
        this.add(content, BorderLayout.CENTER);


        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = list.getSelectedValue();
                if (o != null) {
                    @SuppressWarnings("unchecked")
                    Preset<T> p = (Preset<T>)o;
                    if (JOptionPane.showConfirmDialog(PresetManagerDialog.this,
                            "Are you sure you want to delete the preset \""    + p.getDescription() + "\" ?",
                            "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        presetManager.delete(p);
                        listDataModel.removeElement(o);
                        if (listDataModel.getSize() == 0)
                            setVisible(false);
                    }
                }
            }
        });

        renameButton = new JButton("Rename");
        renameButton.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = list.getSelectedValue();
                if (o != null) {
                    String desc = JOptionPane.showInputDialog(PresetManagerDialog.this,
                            "Please enter the new preset description:", (
                            (Preset<T>)o).getDescription());
                    if (desc!=null) {
                        presetManager.rename((Preset<T>)o, desc);
                    }
                }
            }
        });

        listDataModel = new DefaultListModel();

        for (Preset<T> p : presetManager.list())
            if (!p.isBuiltIn())
                listDataModel.addElement(p);

        listScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        list = new JList(listDataModel);
        listScroll.setViewportView(list);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        content.add(listScroll, "0 0 0 3");
        content.add(okButton, "1 3");
        content.add(deleteButton, "1 0");
        content.add(renameButton, "1 1");
    }
}
