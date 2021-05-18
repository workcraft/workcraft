package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionUtils;
import org.workcraft.gui.controls.FlatTextField;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TextActionCellRenderer extends JPanel implements TableCellRenderer {

    private final JTextField text = new FlatTextField();
    private final JButton leftButton = new JButton();
    private final JButton rightButton = new JButton();

    public TextActionCellRenderer() {
        leftButton.setFocusable(false);
        leftButton.setVisible(false);
        leftButton.setMargin(PropertyHelper.BUTTON_INSETS);

        text.setFocusable(true);

        rightButton.setFocusable(false);
        rightButton.setVisible(false);
        rightButton.setMargin(PropertyHelper.BUTTON_INSETS);

        setLayout(new BorderLayout());
        add(leftButton, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
        add(rightButton, BorderLayout.EAST);
        setFocusable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof TextAction) {
            TextAction textAction = (TextAction) value;

            Action leftAction = textAction.getLeftAction();
            if (leftAction != null) {
                leftButton.setVisible(true);
                leftButton.setText(leftAction.getTitle());
                leftButton.setToolTipText(ActionUtils.getActionTooltip(leftAction));
            }

            text.setFont(table.getFont());
            text.setText(textAction.getText());

            Color foreground = textAction.getForeground();
            if (foreground != null) {
                text.setForeground(foreground);
            }

            Color background = textAction.getBackground();
            if (background != null) {
                text.setBackground(background);
            }

            Action rightAction = textAction.getRightAction();
            if (rightAction != null) {
                rightButton.setVisible(true);
                rightButton.setText(rightAction.getTitle());
                rightButton.setToolTipText(ActionUtils.getActionTooltip(rightAction));
            }
        }
        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event != null) {
            int x = event.getX();
            if (leftButton.isVisible() && (x >= leftButton.getX()) && (x < leftButton.getX() + leftButton.getWidth())) {
                return leftButton.getToolTipText(event);
            }
            if (rightButton.isVisible() && (x >= rightButton.getX()) && (x < rightButton.getX() + rightButton.getWidth())) {
                return rightButton.getToolTipText(event);
            }
        }
        return super.getToolTipText(event);
    }

}
