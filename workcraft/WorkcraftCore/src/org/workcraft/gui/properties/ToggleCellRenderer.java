package org.workcraft.gui.properties;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ToggleCellRenderer extends JPanel implements TableCellRenderer {

    private final Map<String, JToggleButton> optionToToggleButtonMap = new HashMap<>();

    public ToggleCellRenderer() {
        setLayout(new GridLayout());
        setFocusable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        boolean isEnabled = table.isEnabled() && table.isCellEditable(row, column);
        if (value instanceof Toggle toggle) {
            ButtonGroup buttonGroup = new ButtonGroup();
            for (String option : toggle.getOptions()) {
                JToggleButton toggleButton = optionToToggleButtonMap.get(option);
                if (toggleButton == null) {
                    toggleButton = new JToggleButton(option);
                    buttonGroup.add(toggleButton);
                    toggleButton.setFocusable(false);
                    toggleButton.setMargin(PropertyHelper.BUTTON_INSETS);
                    add(toggleButton);
                    optionToToggleButtonMap.put(option, toggleButton);
                }
                toggleButton.setEnabled(isEnabled);
                if (toggle.isSelectedOption(option)) {
                    toggleButton.setSelected(true);
                }
            }
        }
        setEnabled(isEnabled);
        return this;
    }

}
