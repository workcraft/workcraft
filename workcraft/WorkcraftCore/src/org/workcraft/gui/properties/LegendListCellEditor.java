package org.workcraft.gui.properties;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class LegendListCellEditor extends AbstractCellEditor implements TableCellEditor {

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JPanel result = new JPanel(new GridLayout());
        result.setFocusable(false);
        if (value instanceof LegendList) {
            LegendList legendList = (LegendList) value;
            for (Legend legend : legendList) {
                JButton button = new JButton(legend.getTitle());
                button.setToolTipText(legend.getTooltip());
                button.setForeground(legend.getForeground());
                button.setBackground(legend.getBackground());
                button.setFocusable(false);
                button.addActionListener(e -> legend.run());
                result.add(button);
            }
        }
        return result;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

}
