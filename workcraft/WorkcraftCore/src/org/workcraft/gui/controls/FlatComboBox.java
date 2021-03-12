package org.workcraft.gui.controls;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;

public class FlatComboBox extends JComboBox<Object> {

    private static final Color PANEL_BACKGROUND = UIManager.getColor("Panel.background");

    static class FlatComboBoxUI extends BasicComboBoxUI {
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

    static class FlatListCellRenderer implements ListCellRenderer<Object> {
        private final Border insetBorder = GuiUtils.getTableCellBorder();
        private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            JComponent renderer = (JComponent) defaultRenderer.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            renderer.setBorder(insetBorder);
            return renderer;
        }
    }

    public FlatComboBox() {
        setUI(new FlatComboBoxUI());
        setRenderer(new FlatListCellRenderer());
        setFocusable(false);
        setMaximumRowCount(25);
        for (int i = 0; i < getComponentCount(); i++) {
            Component component = getComponent(i);
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
                button.setBorder(new EmptyBorder(0, 0, 0, 0));
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isOpaque()) {
            g.setColor(PANEL_BACKGROUND);
            Rectangle rect = getValueRectangle();
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
    }

    public Rectangle getValueRectangle() {
        Dimension d = getSize();
        AbstractButton arrowButton = getArrowButton();
        if (arrowButton != null) {
            d.width -= arrowButton.getWidth();
        }
        return new Rectangle(d);
    }

    public AbstractButton getArrowButton() {
        for (Component component : getComponents()) {
            if (component instanceof AbstractButton) {
                return (AbstractButton) component;
            }
        }
        return null;
    }

}
