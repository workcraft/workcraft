package org.workcraft.gui.properties;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class LegendListCellRenderer extends JPanel implements TableCellRenderer {

    private final Map<Legend, JButton> legendButtonMap = new HashMap<>();

    public LegendListCellRenderer() {
        setLayout(new GridLayout());
        setFocusable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof LegendList) {
            LegendList legendList = (LegendList) value;
            for (Legend legend : legendList) {
                if (!legendButtonMap.containsKey(legend)) {
                    JButton button = new JButton();
                    button.setText(legend.getTitle());
                    button.setToolTipText(legend.getTooltip());
                    button.setForeground(legend.getForeground());
                    button.setBackground(legend.getBackground());
                    button.setFocusable(false);
                    button.setMargin(PropertyHelper.BUTTON_INSETS);
                    add(button);
                    legendButtonMap.put(legend, button);
                }
            }
        }
        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event != null) {
            int x = event.getX();
            for (JButton button : legendButtonMap.values()) {
                if ((x >= button.getX()) && (x < button.getX() + button.getWidth())) {
                    return button.getToolTipText(event);
                }
            }
        }
        return super.getToolTipText();
    }

}
