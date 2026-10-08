package org.workcraft.gui.controls;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FlatTextField extends JTextField {

    public FlatTextField() {
        this(null);
    }

    public FlatTextField(String text) {
        super(text);
        // Text field places text 1px lower than label, so shift it up to match FlatLabel
        Insets insets = GuiUtils.getTableCellBorder().getBorderInsets(this);
        setBorder(new EmptyBorder(Math.max(insets.top - 1, 0), insets.left, insets.bottom + 1, insets.right));
    }

}