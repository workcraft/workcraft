package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatComboBox;

import javax.swing.*;
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

    public ChoiceCellEditor(PropertyDescriptor<Object> descriptor) {
        comboBox = new FlatComboBox();
        comboBox.setFocusable(false);
        comboBox.addItemListener(this);

        Map<Object, String> choice = descriptor.getChoice();
        wrappers = new ArrayList<>();
        for (Map.Entry<Object, String> entry : choice.entrySet()) {
            ChoiceWrapper wrapper = new ChoiceWrapper(entry.getKey(), entry.getValue());
            wrappers.add(wrapper);
            comboBox.addItem(wrapper);
        }
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

