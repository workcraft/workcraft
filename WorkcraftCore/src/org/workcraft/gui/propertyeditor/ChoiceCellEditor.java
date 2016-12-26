package org.workcraft.gui.propertyeditor;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class ChoiceCellEditor extends AbstractCellEditor implements TableCellEditor, ItemListener {
    private final JComboBox comboBox;
    private final ChoiceWrapper[] wrappers;

    public ChoiceCellEditor(PropertyDescriptor decl) {
        comboBox = new JComboBox();
        comboBox.setEditable(false);
        comboBox.setFocusable(false);
        comboBox.addItemListener(this);
        comboBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        int declCount = decl.getChoice().size();
        wrappers = new ChoiceWrapper[declCount];
        int j = 0;
        for (Object o : decl.getChoice().keySet()) {
            wrappers[j] = new ChoiceWrapper(decl.getChoice().get(o), o);
            comboBox.addItem(wrappers[j]);
            j++;
        }
    }

    public Object getCellEditorValue() {
        return ((ChoiceWrapper) comboBox.getSelectedItem()).value;
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        // FIXME: This is a hack to enable choice operation in a group
        // selection with different initial values of the corresponding
        // property. Without this hack a selection of the first item in
        // the combo-box does not take effect, however selection of any
        // other item is properly recognised.
        // * First select an "incorrect" item
        for (ChoiceWrapper w : wrappers) {
            if (!w.text.equals(value)) {
                comboBox.setSelectedItem(w);
            }
        }
        // * Then select a "correct" item
        for (ChoiceWrapper w : wrappers) {
            if (w.text.equals(value)) {
                comboBox.setSelectedItem(w);
            }
        }
        comboBox.setOpaque(value == null);
        comboBox.setFont(table.getFont());
        return comboBox;
    }

    public void itemStateChanged(ItemEvent e) {
        fireEditingStopped();
    }
}

