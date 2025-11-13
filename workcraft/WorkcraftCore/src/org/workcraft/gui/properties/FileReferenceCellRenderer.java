package org.workcraft.gui.properties;

import org.workcraft.dom.references.FileReference;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;

public class FileReferenceCellRenderer extends JPanel implements TableCellRenderer {

    private final JButton enterButton;
    private final JButton chooseButton;
    private final JButton clearButton;

    public FileReferenceCellRenderer() {
        enterButton = new JButton(PropertyHelper.ENTER_SYMBOL);
        enterButton.setToolTipText("Open");
        enterButton.setEnabled(false);
        enterButton.setFocusable(false);
        enterButton.setMargin(PropertyHelper.BUTTON_INSETS);

        chooseButton = new JButton();
        chooseButton.setBorderPainted(false);
        chooseButton.setFocusable(false);
        chooseButton.setOpaque(true);
        chooseButton.setMargin(PropertyHelper.BUTTON_INSETS);
        chooseButton.setHorizontalAlignment(SwingConstants.LEFT);

        clearButton = new JButton(PropertyHelper.CLEAR_SYMBOL);
        clearButton.setToolTipText("Clear");
        clearButton.setEnabled(false);
        clearButton.setFocusable(false);
        clearButton.setMargin(PropertyHelper.BUTTON_INSETS);

        setLayout(new BorderLayout());
        add(enterButton, BorderLayout.WEST);
        add(chooseButton, BorderLayout.CENTER);
        add(clearButton, BorderLayout.EAST);
        setFocusable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        enterButton.setEnabled(false);
        chooseButton.setFont(table.getFont());
        chooseButton.setText(null);
        clearButton.setEnabled(false);
        chooseButton.setToolTipText(null);

        if (value instanceof FileReference fileReference) {
            int buttonSize = table.getRowHeight(row);
            Dimension buttonDimension = new Dimension(buttonSize, buttonSize);
            enterButton.setPreferredSize(buttonDimension);
            clearButton.setPreferredSize(buttonDimension);

            File file = fileReference.getFile();
            if (file != null) {
                if (file.exists()) {
                    enterButton.setEnabled(true);
                    chooseButton.setForeground(Color.BLACK);
                } else {
                    chooseButton.setForeground(Color.RED);
                }

                chooseButton.setText(file.getName());
                chooseButton.setToolTipText(file.getAbsolutePath());
                clearButton.setEnabled(true);
            }

            Color background = isSelected ? table.getSelectionBackground() : table.getBackground();
            setBorder(new MatteBorder(0, 0, 0, 0, background));
        }
        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event != null) {
            int x = event.getX();
            if ((x >= enterButton.getX()) && (x < enterButton.getX() + enterButton.getWidth())) {
                return enterButton.getToolTipText(event);
            }
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
