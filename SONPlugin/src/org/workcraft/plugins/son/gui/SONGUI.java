package org.workcraft.plugins.son.gui;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.util.GUI;

public class SONGUI extends GUI{

    public static JToggleButton createIconToggleButton(Icon icon, String toolTip) {
        JToggleButton result = new JToggleButton(icon);
        result.setToolTipText(toolTip);
        result.setMargin(new Insets(0,0,0,0));
        int iconSize = CommonEditorSettings.getIconSize();
        Insets insets = result.getInsets();
        int minSize = iconSize+Math.max(insets.left+insets.right, insets.top+insets.bottom);
        result.setPreferredSize(new Dimension(minSize, minSize));

        return result;
    }

}
