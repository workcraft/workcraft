package org.workcraft.gui.properties;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;

@SuppressWarnings("serial")
public class FileCellRenderer extends JPanel implements TableCellRenderer {

    private final JButton chooseButton;

    public FileCellRenderer() {
        chooseButton = new JButton();
        chooseButton.setBorderPainted(false);
        chooseButton.setFocusable(false);
        chooseButton.setOpaque(true);
        chooseButton.setMargin(PropertyHelper.BUTTON_INSETS);
        chooseButton.setHorizontalAlignment(SwingConstants.LEFT);

        JButton clearButton = new JButton(PropertyHelper.CLEAR_SYMBOL);
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
        setToolTipText(null);

        if (value instanceof File) {
            File file = (File) value;
            if (file.exists()) {
                chooseButton.setForeground(Color.BLACK);
            } else {
                chooseButton.setForeground(Color.RED);
            }

            chooseButton.setText(file.getPath());
            setToolTipText(file.getAbsolutePath());

            Color background = isSelected ? table.getSelectionBackground() : table.getBackground();
            setBorder(new MatteBorder(0, 0, 0, 0, background));
        }
        return this;
    }

}
