package org.workcraft.gui.properties;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

class ColorComboBoxRenderer extends DefaultListCellRenderer {

    private static final int HIGHLIGHT_WIDTH = 2;

    // Item is the current value of the combo box (as opposed to the item highlighted in the list)
    private boolean current = false;

    @Override
    public void paint(Graphics g) {
        GuiUtils.paintBackgroundColor(g, new Rectangle(getSize()), getBackground());
        paintBorder(g);
        if (current && (g instanceof Graphics2D g2)) {
            paintCheckMark(g2);
        }
    }

    private void paintCheckMark(Graphics2D g2) {
        Color color = getBackground();
        boolean isDark = (color.getAlpha() > 0)
                && ((0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) < 128);

        // Same size and position as in other combo boxes: at the right edge, after the cell insets
        int checkWidth = getFontMetrics(getFont()).getHeight();
        int size = Math.max(checkWidth / 4, 2);
        int x = getWidth() - GuiUtils.getTableCellBorder().getBorderInsets(this).right - checkWidth / 2;
        int y = getHeight() / 2;
        Stroke stroke = g2.getStroke();
        Object antialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isDark ? Color.WHITE : Color.BLACK);
        g2.setStroke(new BasicStroke(Math.max(checkWidth / 8f, 1f)));
        g2.drawPolyline(
                new int[]{x - size, x - size / 3, x + size},
                new int[]{y, y + size * 2 / 3, y - size * 2 / 3},
                3);
        g2.setStroke(stroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, " ", index, isSelected, cellHasFocus);

        if (value instanceof Color) {
            setBackground((Color) value);
        }
        // Index is negative when the renderer is used for the value displayed in the combo box itself
        current = (index >= 0) && (value != null)
                && (list.getModel() instanceof ComboBoxModel<?> model) && value.equals(model.getSelectedItem());

        // Background is the colour itself, so the highlighted item is shown by a border
        setBorder((isSelected && (index >= 0))
                ? BorderFactory.createLineBorder(list.getSelectionBackground(), HIGHLIGHT_WIDTH)
                : BorderFactory.createEmptyBorder());
        return this;
    }

}
