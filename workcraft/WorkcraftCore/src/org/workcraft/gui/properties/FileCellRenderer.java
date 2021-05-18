package org.workcraft.gui.properties;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;

public class FileCellRenderer extends JPanel implements TableCellRenderer {

    private final JButton chooseButton;
    private final JButton clearButton;

    public FileCellRenderer() {
        chooseButton = new JButton();
        chooseButton.setBorderPainted(false);
        chooseButton.setFocusable(false);
        chooseButton.setOpaque(true);
        chooseButton.setMargin(PropertyHelper.BUTTON_INSETS);
        chooseButton.setHorizontalAlignment(SwingConstants.LEFT);

        clearButton = new JButton(PropertyHelper.CLEAR_SYMBOL);
        clearButton.setToolTipText("Clear");
        clearButton.setFocusable(false);
        clearButton.setMargin(PropertyHelper.BUTTON_INSETS);

        setLayout(new BorderLayout());
        add(chooseButton, BorderLayout.CENTER);
        add(clearButton, BorderLayout.EAST);
        setFocusable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        chooseButton.setFont(table.getFont());
        chooseButton.setText(null);
        chooseButton.setToolTipText(null);

        if (value instanceof File) {
            File file = (File) value;
            if (file.exists()) {
                chooseButton.setForeground(Color.BLACK);
            } else {
                chooseButton.setForeground(Color.RED);
            }

            chooseButton.setText(file.getPath());
            chooseButton.setToolTipText(file.getAbsolutePath());

            Color background = isSelected ? table.getSelectionBackground() : table.getBackground();
            setBorder(new MatteBorder(0, 0, 0, 0, background));
        }
        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event != null) {
            int x = event.getX();
            if ((x >= chooseButton.getX()) && (x < chooseButton.getX() + chooseButton.getWidth())) {
                return chooseButton.getToolTipText(event);
            }
            if ((x >= clearButton.getX()) && (x < clearButton.getX() + clearButton.getWidth())) {
                return clearButton.getToolTipText(event);
            }
        }
        return super.getToolTipText(event);
    }

}
