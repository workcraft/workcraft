package org.workcraft.gui.properties;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.controls.FlatComboBox;
import org.workcraft.gui.controls.FlatTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.Map;

public class BasicCellEditor extends AbstractCellEditor implements TableCellEditor {

    private static final Color INVALID_TEXT_COLOR = Color.RED;

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

    // Text field where the value is typed (the component itself or the editor of the combo box)
    private final JTextField textField;

    // Property class that checks the typed text (its conversion throws ArgumentException if the text is not a valid
    // value), or null to accept any text
    private final AbstractBasicProperty<?> property;

    // Table this editor was last activated in
    private JTable table = null;

    // Selection is being set up for a new edit, so it must not stop that edit
    private boolean initialising = false;

    // Typed text is being applied as part of stopping the edit, so it must not stop the edit again
    private boolean committing = false;

    // Text is marked as invalid
    private boolean invalid = false;

    // Stopping the edit was refused because of invalid text, so from now on the typed text must be a complete value
    private boolean refused = false;

    private Color validTextColor = null;

    public BasicCellEditor() {
        this(null, null);
    }

    public BasicCellEditor(Map<?, String> predefinedValues, AbstractBasicProperty<?> property) {
        this.property = property;
        if (predefinedValues == null) {
            FlatTextField flatTextField = new FlatTextField();
            component = flatTextField;
            textField = flatTextField;
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
            textField = (JTextField) comboBox.getEditor().getEditorComponent();
            // Selected item is the text of the cell value, while the list items are wrappers of the choices
            comboBox.setCurrentItemMatcher((item, selected) ->
                    (item instanceof ChoiceWrapper wrapper) && (wrapper.object != null)
                            && wrapper.object.toString().equals(selected));
            // Arrow keys move the highlight in the list without changing the selected item (as in DefaultCellEditor)
            comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
            // Focus can leave the editor text field while the drop-down list is open (e.g. click on canvas)
            textField.addFocusListener(new FocusAdapter() {
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
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                recheck();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                recheck();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
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
        // Value is only read for checking if there is a property class (subclasses may convert it with side effects)
        String error = (property == null) ? null : getError(getCellEditorValue());
        if (error != null) {
            // Keep the edit open with the typed text, so that it can be corrected
            refused = true;
            setInvalid(true, error);
            return false;
        }
        setInvalid(false, null);
        return super.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        refused = false;
        setInvalid(false, null);
        super.cancelCellEditing();
    }

    private String getError(Object value) {
        if (value instanceof String text) {
            try {
                property.fromCellEditorValue(text);
            } catch (ArgumentException e) {
                return e.getMessage();
            }
        }
        return null;
    }

    private void recheck() {
        if (property != null) {
            String text = textField.getText();
            // While typing, only text that cannot be completed to a valid value is marked (e.g. "-" is not)
            String error = (refused || !property.isValidBeginning(text)) ? getError(text) : null;
            setInvalid(error != null, error);
        }
    }

    private void setInvalid(boolean value, String error) {
        if (value && !invalid) {
            validTextColor = textField.getForeground();
        }
        if (value) {
            textField.setForeground(INVALID_TEXT_COLOR);
            textField.setToolTipText(error);
        } else if (invalid) {
            textField.setForeground(validTextColor);
            textField.setToolTipText(null);
        }
        invalid = value;
    }

    @Override
    public Object getCellEditorValue() {
        if (component instanceof FlatTextField flatTextField) {
            return flatTextField.getText();
        }
        if (component instanceof FlatComboBox comboBox) {
            Object item = comboBox.getSelectedItem();
            if (item instanceof ChoiceWrapper choiceWrapper) {
                return choiceWrapper.object.toString();
            } else {
                return item;
            }
        }
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        refused = false;
        setInvalid(false, null);
        if ((component instanceof FlatTextField flatTextField) && (value instanceof String stringValue)) {
            flatTextField.setText(stringValue);
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
