package org.workcraft.gui.controls;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class WrapHeaderRenderer extends JTextPane implements TableCellRenderer {

    public WrapHeaderRenderer() {
        setEditable(false);
        setOpaque(false);
        setFocusable(false);
        setBorder(GuiUtils.getTableHeaderBorder());
        setDocumentAlignment(StyleConstants.ALIGN_CENTER);

    }

    private void setDocumentAlignment(int alignment) {
        StyledDocument doc = getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, alignment);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        setText(value.toString());
        int width = table.getColumnModel().getColumn(column).getWidth();
        int height = getPreferredSize().height;
        setSize(width, height);
        return this;
    }

}
