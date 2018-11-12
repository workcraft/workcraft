package org.workcraft.gui.properties;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class FlatComboBox extends JComboBox {

    private static final Color PANEL_BACKGROUND = UIManager.getColor("Panel.background");

    class FlatComboBoxUI extends BasicComboBoxUI {
        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox) {
                @Override
                protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
                    return super.computePopupBounds(px, py, Math.max(comboBox.getPreferredSize().width, pw), ph);
                }
            };
            popup.getAccessibleContext().setAccessibleParent(comboBox);
            return popup;
        }
    }

    public FlatComboBox() {
        setUI(new FlatComboBoxUI());
        setFocusable(false);
        setMaximumRowCount(25);
        EmptyBorder emptyBorder = new EmptyBorder(0, 0, 0, 0);
        for (int i = 0; i < getComponentCount(); i++) {
            Component component = getComponent(i);
            if (component instanceof JComponent) {
                ((JComponent) component).setBorder(emptyBorder);
            }
            if (component instanceof AbstractButton) {
                ((AbstractButton) component).setBorderPainted(false);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isOpaque()) {
            g.setColor(PANEL_BACKGROUND);
            Dimension d = getSize();
            g.fillRect(0, 0, d.width, d.height);
        }
    }

}
