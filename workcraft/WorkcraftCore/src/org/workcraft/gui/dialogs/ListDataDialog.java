package org.workcraft.gui.dialogs;

import org.workcraft.gui.lists.MultipleListSelectionModel;
import org.workcraft.presets.DataPreserver;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.SortUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ListDataDialog extends ModalDialog<DataPreserver<List<String>>> {

    private ItemList itemList;

    class ItemList extends JList<String> {

        ItemList(Collection<String> items) {
            super(SortUtils.getSortedNatural(items).toArray(new String[0]));
            setBorder(GuiUtils.getEmptyBorder());
            setSelectionModel(new MultipleListSelectionModel());
            setCellRenderer(getItemListCellRenderer());
        }
    }

    public ListDataDialog(Window owner, String title, DataPreserver<List<String>> userData) {
        super(owner, title, userData);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                requestFocus();
            }
        });
    }

    @Override
    public JPanel createContentPanel() {
        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.setBorder(GuiUtils.getEmptyBorder());

        itemList = new ItemList(getItems());
        selectListItems(itemList, getUserData().loadData());

        JButton clearButton = new JButton("Clear selection");
        clearButton.addActionListener(event -> itemList.clearSelection());

        result.add(new JLabel(getSelectionPrompt()), BorderLayout.NORTH);
        result.add(new JScrollPane(itemList), BorderLayout.CENTER);
        result.add(clearButton, BorderLayout.SOUTH);
        return result;
    }

    public String getSelectionPrompt() {
        return "Select exceptions:";
    }

    private void selectListItems(ItemList itemList, List<String> items) {
        ListModel<String> itemListModel = itemList.getModel();
        List<Integer> indices = new ArrayList<>();
        for (int index = 0; index < itemListModel.getSize(); index++) {
            String item = itemListModel.getElementAt(index);
            if (items.contains(item)) {
                indices.add(index);
            }
        }
        // Convert ArrayList<Integer> to int[]
        int[] itemsToSelect = indices.stream().mapToInt(i -> i).toArray();
        itemList.setSelectedIndices(itemsToSelect);
    }

    @Override
    public boolean okAction() {
        boolean result = super.okAction();
        if (result) {
            getUserData().saveData(itemList.getSelectedValuesList());
        }
        return result;
    }

    public DefaultListCellRenderer getItemListCellRenderer() {
        return new DefaultListCellRenderer();
    }

    public abstract Collection<String> getItems();

}
