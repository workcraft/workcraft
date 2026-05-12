package org.workcraft.gui.dialogs;

import org.workcraft.gui.lists.ColorListCellRenderer;
import org.workcraft.gui.lists.MultipleListSelectionModel;
import org.workcraft.presets.DataPreserver;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.SortUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

public abstract class ListDataDialog extends ModalDialog<DataPreserver<List<String>>> {

    private ItemList itemList;
    private Set<String> invalidItems;

    class ItemList extends JList<String> {
        private final DefaultListModel<String> listModel;

        ItemList(Collection<String> validItems, Collection<String> invalidItems) {
            super();
            listModel = new DefaultListModel<>();
            setModel(listModel);
            listModel.addAll(SortUtils.getSortedNatural(invalidItems));
            listModel.addAll(SortUtils.getSortedNatural(validItems));

            setBorder(GuiUtils.getEmptyBorder());
            setSelectionModel(new MultipleListSelectionModel());

            ColorListCellRenderer cellRenderer = new ColorListCellRenderer(ListDataDialog.this::getItemColorOrNullForInvalid);
            cellRenderer.setInvalidItemTooltip("Outdated exception will be ignored if selected and removed if unselected");
            setCellRenderer(cellRenderer);
        }

        @Override
        public DefaultListModel<String> getModel() {
            return listModel;
        }

        boolean isEmpty() {
            return listModel.isEmpty();
        }

        boolean hasSelectedItems() {
            return !getSelectedValuesList().isEmpty();
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

        Collection<String> validItems = getItems();
        List<String> selectedItems = getUserData().loadData();
        invalidItems = new HashSet<>(selectedItems);
        invalidItems.removeAll(validItems);
        itemList = new ItemList(validItems, invalidItems);
        loadSelection();

        JButton clearSelectionButton = new JButton("Clear selection");
        clearSelectionButton.addActionListener(event -> itemList.clearSelection());
        clearSelectionButton.setEnabled(itemList.hasSelectedItems());
        itemList.addListSelectionListener(event ->
                clearSelectionButton.setEnabled(itemList.hasSelectedItems()));

        result.add(new JLabel(getSelectionPrompt()), BorderLayout.NORTH);
        result.add(new JScrollPane(itemList), BorderLayout.CENTER);
        result.add(clearSelectionButton, BorderLayout.SOUTH);
        return result;
    }

    public String getSelectionPrompt() {
        return "Select exceptions:";
    }

    private void loadSelection() {
        List<String> selectionItems = getUserData().loadData();
        DefaultListModel<String> listModel = itemList.getModel();
        List<Integer> selectionIndices = new ArrayList<>();
        for (int index = 0; index < listModel.getSize(); index++) {
            String item = listModel.getElementAt(index);
            if (selectionItems.contains(item)) {
                selectionIndices.add(index);
            }
        }
        // Convert ArrayList<Integer> to int[]
        int[] itemsToSelect = selectionIndices.stream().mapToInt(i -> i).toArray();
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

    public Color getItemColorOrNullForInvalid(Object item) {
        return isValidItem(item) ? itemList.getForeground() : null;
    }

    public boolean isValidItem(Object item) {
        return (item instanceof String str) && !invalidItems.contains(str);
    }

    @Override
    public boolean hasData() {
        return !itemList.isEmpty();
    }

    public abstract Collection<String> getItems();

}
