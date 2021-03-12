package org.workcraft.gui.controls;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;

public class FlatTextField extends JTextField {

    public FlatTextField() {
        this(null);
    }

    public FlatTextField(String text) {
        super(text);
        setBorder(GuiUtils.getTableCellBorder());
    }

}
