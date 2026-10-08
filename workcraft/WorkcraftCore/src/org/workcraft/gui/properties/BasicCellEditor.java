package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatComboBox;
import org.workcraft.gui.controls.FlatTextField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.Map;

public class BasicCellEditor extends AbstractCellEditor implements TableCellEditor {

    static class ChoiceWrapper {
        public Object object;
        public String description;

        ChoiceWrapper(Object object, String description) {
            this.object = object;
            this.description = description;
        }

        @Override
        public String toString() {
            String prefix = (object == null) ? "" : object.toString();
            String delimiter = (object == null) || (description == null) || description.isEmpty() ? "" : " &ndash; ";
            String suffix = (description == null) || description.isEmpty() ? "" : ("<i>" + description + "</i>");
            return "<html>" + prefix + delimiter + suffix + "</html>";
        }
    }

    private final JComponent component;

    // Table this editor was last activated in
    private JTable table = null;

    // Selection is being set up for a new edit, so it must not stop that edit
    private boolean initialising = false;

    // Typed text is being applied as part of stopping the edit, so it must not stop the edit again
    private boolean committing = false;

    public BasicCellEditor() {
        this(null);
    }

    public BasicCellEditor(Map<?, String> predefinedValues) {
        if (predefinedValues == null) {
            component = new FlatTextField();
            component.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    PropertyEditorTable.stopEditingOnFocusLost(table, BasicCellEditor.this, e);
                }
            });
        } else {
            component = new FlatComboBox();
            FlatComboBox comboBox = (FlatComboBox) component;
            comboBox.setEditable(true);
            // Selected item is the text of the cell value, while the list items are wrappers of the choices
            comboBox.setCurrentItemMatcher((item, selected) ->
                    (item instanceof ChoiceWrapper wrapper) && (wrapper.object != null)
                            && wrapper.object.toString().equals(selected));
            // Arrow keys move the highlight in the list without changing the selected item (as in DefaultCellEditor)
            comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
            // Focus can leave the editor text field while the drop-down list is open (e.g. click on canvas)
            comboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    PropertyEditorTable.stopEditingOnFocusLost(table, BasicCellEditor.this, e);
                }
            });

            for (Map.Entry<?, String> entry : predefinedValues.entrySet()) {
                comboBox.addItem(new ChoiceWrapper(entry.getKey(), entry.getValue()));
            }

            comboBox.addItemListener(e -> {
                if ((e.getStateChange() == ItemEvent.SELECTED) && !initialising && !committing) {
                    stopCellEditing();
                }
            });
            // Enter in the editor text field only fires an action (and no item event) if the item is unchanged
            comboBox.addActionListener(e -> {
                if (!initialising && !committing) {
                    stopCellEditing();
                }
            });
        }
        component.setFocusable(true);
    }

    @Override
    public boolean stopCellEditing() {
        if (component instanceof FlatComboBox comboBox) {
            // Apply typed text, as it is otherwise lost when edit is stopped by clicking on another cell
            committing = true;
            try {
                comboBox.commitTypedText();
            } finally {
                committing = false;
            }
        }
        return super.stopCellEditing();
    }

    @Override
    public Object getCellEditorValue() {
        if (component instanceof FlatTextField textField) {
            return textField.getText();
        }
        if (component instanceof FlatComboBox comboBox) {
            Object item = comboBox.getSelectedItem();
            if (item instanceof ChoiceWrapper wrapper) {
                return wrapper.object.toString();
            } else {
                return item;
            }
        }
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;

        if ((component instanceof FlatTextField textField) && (value instanceof String text)) {
            textField.setText(text);
        }
        if (component instanceof FlatComboBox comboBox) {
            initialising = true;
            try {
                comboBox.setSelectedItem(value);
            } finally {
                initialising = false;
            }
        }
        component.setOpaque(value == null);
        component.setFont(table.getFont());
        return component;
    }

}
