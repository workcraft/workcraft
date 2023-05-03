package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatComboBox;
import org.workcraft.gui.controls.FlatTextField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

public class BasicCellEditor extends AbstractCellEditor implements TableCellEditor, ItemListener {

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

    public BasicCellEditor() {
        this(null);
    }

    public BasicCellEditor(Map<?, String> predefinedValues) {
        if (predefinedValues == null) {
            component = new FlatTextField();
            component.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (!e.isTemporary()) {
                        stopCellEditing();
                    }
                }
            });
        } else {
            component = new FlatComboBox();
            FlatComboBox comboBox = (FlatComboBox) component;
            comboBox.setEditable(true);

            for (Map.Entry<? extends Object, String> entry : predefinedValues.entrySet()) {
                comboBox.addItem(new ChoiceWrapper(entry.getKey(), entry.getValue()));
            }

            comboBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    stopCellEditing();
                }
            });
        }
        component.setFocusable(true);
    }

    @Override
    public Object getCellEditorValue() {
        if (component instanceof FlatTextField) {
            return ((FlatTextField) component).getText();
        }
        if (component instanceof FlatComboBox) {
            Object item = ((FlatComboBox) component).getSelectedItem();
            if (item instanceof ChoiceWrapper) {
                ChoiceWrapper wrapper = (ChoiceWrapper) item;
                return wrapper.object.toString();
            } else {
                return item;
            }
        }
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (component instanceof FlatTextField) {
            ((FlatTextField) component).setText((String) value);
        }
        if (component instanceof FlatComboBox) {
            ((FlatComboBox) component).setSelectedItem(value);
        }
        component.setOpaque(value == null);
        component.setFont(table.getFont());
        return component;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        fireEditingStopped();
    }

}
