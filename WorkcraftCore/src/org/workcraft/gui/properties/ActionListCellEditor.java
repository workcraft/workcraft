package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

@SuppressWarnings("serial")
public class ActionListCellEditor extends AbstractCellEditor implements TableCellEditor {

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JPanel actionPanel = new JPanel();
        if (value instanceof ActionList) {
            ActionList actions = (ActionList) value;
            actionPanel.setLayout(new GridLayout(1, actions.size()));
            for (Action action : actions) {
                JButton actionButton = new JButton(action.getText());
                actionButton.addActionListener(e -> action.run());
                actionPanel.add(actionButton);
            }
        }
        return actionPanel;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

}
