package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatComboBox;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChoiceCellEditor extends AbstractCellEditor implements TableCellEditor, ItemListener {

    static class ChoiceWrapper {
        public Object object;
        public String description;

        ChoiceWrapper(Object object, String description) {
            this.object = object;
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    private final FlatComboBox comboBox;
    private final List<ChoiceWrapper> wrappers;

    // Table this editor was last activated in
    private JTable table = null;

    public ChoiceCellEditor(PropertyDescriptor<Object> descriptor) {
        comboBox = new FlatComboBox();
        comboBox.setFocusable(false);
        // Arrow keys move the highlight in the list without changing the selected item (as in DefaultCellEditor)
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        // Take focus only while the drop-down list is visible, so that it can be navigated by keyboard
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                comboBox.setFocusable(true);
                comboBox.requestFocusInWindow();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                comboBox.setFocusable(false);
                cancelEditingIfNoChoice();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                comboBox.setFocusable(false);
                cancelEditingIfNoChoice();
            }
        });
        comboBox.addItemListener(this);

        Map<Object, String> choice = descriptor.getChoice();
        wrappers = new ArrayList<>();
        for (Map.Entry<Object, String> entry : choice.entrySet()) {
            ChoiceWrapper wrapper = new ChoiceWrapper(entry.getKey(), entry.getValue());
            wrappers.add(wrapper);
            comboBox.addItem(wrapper);
        }
    }

    private void cancelEditingIfNoChoice() {
        // Deferred, as the item (if any) is chosen while the list is being closed. If the edit has been stopped
        // (item chosen) or replaced (another cell clicked), there is nothing to do. Otherwise list was dismissed
        // (e.g. by clicking on canvas), so the edit would hang and block updates of the property editor.
        SwingUtilities.invokeLater(() -> PropertyEditorTable.cancelEditingIfActive(table, this));
    }

    @Override
    public Object getCellEditorValue() {
        Object selectedItem = comboBox.getSelectedItem();
        if (selectedItem instanceof ChoiceWrapper) {
            return ((ChoiceWrapper) selectedItem).object;
        }
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        comboBox.setOpaque(value == null);
        comboBox.setFont(table.getFont());
        // First select non-existent item, then select a "correct" item.
        comboBox.setSelectedItem(null);
        for (ChoiceWrapper wrapper: wrappers) {
            if (wrapper.description.equals(value)) {
                comboBox.setSelectedItem(wrapper);
                break;
            }
        }
        return comboBox;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        fireEditingStopped();
    }

}

