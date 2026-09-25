package org.workcraft.gui.properties;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ToggleCellEditor extends AbstractCellEditor implements TableCellEditor {

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JPanel result = new JPanel(new GridLayout());
        result.setFocusable(false);
        if (value instanceof Toggle toggle) {
            ButtonGroup buttonGroup = new ButtonGroup();
            for (String option : toggle.getOptions()) {
                JToggleButton toggleButton = new JToggleButton(option);
                buttonGroup.add(toggleButton);
                toggleButton.setFocusable(false);
                toggleButton.setMargin(PropertyHelper.BUTTON_INSETS);
                if (toggle.isSelectedOption(option)) {
                    toggleButton.setSelected(true);
                }
                toggleButton.addActionListener(e -> toggle.apply(option));
                result.add(toggleButton);
            }
        }
        return result;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

}
