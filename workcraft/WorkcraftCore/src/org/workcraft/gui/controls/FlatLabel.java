package org.workcraft.gui.controls;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;

public class FlatLabel extends JLabel {

    public FlatLabel() {
        this(null);
    }

    public FlatLabel(String text) {
        super(text);
        setBorder(GuiUtils.getTableCellBorder());
    }

}
