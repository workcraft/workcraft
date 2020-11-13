package org.workcraft.gui.controls;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ColorHeaderRenderer extends JLabel implements TableCellRenderer {

    public ColorHeaderRenderer(Color color) {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setBackground(color);
        setBorder(new EtchedBorder());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        setText(value.toString());
        return this;
    }

}

