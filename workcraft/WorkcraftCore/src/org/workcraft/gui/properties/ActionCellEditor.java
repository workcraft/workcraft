package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JButton actionButton = new JButton();
        actionButton.setMargin(PropertyHelper.BUTTON_INSETS);
        if (value instanceof Action action) {
            actionButton.setText(action.getTitle());
            actionButton.addActionListener(e -> action.run());
        }
        return actionButton;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

}
