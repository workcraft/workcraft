package org.workcraft.presets;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class PresetManagerDialog<T> extends JDialog {

    private static final String DIALOG_DELETE_PRESET = "Delete preset";
    private final JList list;
    private final DefaultListModel listDataModel;

    public PresetManagerDialog(Window owner, final PresetManager<T> presetManager) {
        super(owner, "Manage presets");

        JPanel content = new JPanel(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL, 100},
                new double[]{20, 20, TableLayout.FILL, 20}));

        content.setBorder(SizeHelper.getEmptyBorder());

        this.setLayout(new BorderLayout());
        this.add(content, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(event -> setVisible(false));

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = list.getSelectedValue();
                if (o != null) {
                    Preset<T> p = (Preset<T>) o;
                    String msg = "Are you sure you want to delete the preset \'" + p.getDescription() + "\'?";
                    if (DialogUtils.showConfirm(msg, DIALOG_DELETE_PRESET, false)) {
                        presetManager.delete(p);
                        listDataModel.removeElement(o);
                        if (listDataModel.getSize() == 0) {
                            setVisible(false);
                        }
                    }
                }
            }
        });

        JButton renameButton = new JButton("Rename");
        renameButton.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = list.getSelectedValue();
                if (o != null) {
                    String initial = ((Preset<T>) o).getDescription();
                    String desc = DialogUtils.showInput("Please enter the new preset description:", initial);
                    if (desc != null) {
                        presetManager.rename((Preset<T>) o, desc);
                    }
                }
            }
        });

        listDataModel = new DefaultListModel();

        for (Preset<T> p : presetManager.list()) {
            if (!p.isBuiltIn()) {
                listDataModel.addElement(p);
            }
        }

        JScrollPane listScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        list = new JList(listDataModel);
        listScroll.setViewportView(list);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        content.add(listScroll, new TableLayoutConstraints(0, 0, 0, 3));
        content.add(okButton, new TableLayoutConstraints(1, 3));
        content.add(deleteButton, new TableLayoutConstraints(1, 0));
        content.add(renameButton, new TableLayoutConstraints(1, 1));
    }

}
