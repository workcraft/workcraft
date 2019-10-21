package org.workcraft.gui.properties;

import org.workcraft.dom.references.FileReference;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class FileReferenceCellRenderer extends JPanel implements TableCellRenderer {

    private final JButton enterButton;
    private final JButton chooseButton;
    private final JButton clearButton;

    public FileReferenceCellRenderer() {
        enterButton = new JButton(PropertyHelper.ENTER_SYMBOL);
        enterButton.setFocusable(false);
        enterButton.setMargin(PropertyHelper.BUTTON_INSETS);

        chooseButton = new JButton();
        chooseButton.setBorderPainted(false);
        chooseButton.setFocusable(false);
        chooseButton.setOpaque(true);
        chooseButton.setMargin(PropertyHelper.BUTTON_INSETS);
        chooseButton.setHorizontalAlignment(SwingConstants.LEFT);

        clearButton = new JButton(PropertyHelper.CLEAR_SYMBOL);
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
        setToolTipText(null);

        if (value instanceof FileReference) {
            FileReference fileReference = (FileReference) value;
            File file = fileReference.getFile();
            if (file != null) {
                if (file.exists()) {
                    enterButton.setEnabled(true);
                    chooseButton.setForeground(Color.BLACK);
                } else {
                    chooseButton.setForeground(Color.RED);
                }

                chooseButton.setText(file.getName());
                try {
                    setToolTipText(file.getCanonicalPath());
                } catch (IOException e) {
                }
                clearButton.setEnabled(true);
            }

            Color background = isSelected ? table.getSelectionBackground() : table.getBackground();
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, background));
        }
        return this;
    }

}
