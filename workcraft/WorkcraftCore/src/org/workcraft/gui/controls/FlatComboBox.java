package org.workcraft.gui.controls;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionListener;

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

    class FlatListCellRenderer implements ListCellRenderer<Object> {
        private final Border insetBorder = GuiUtils.getTableCellBorder();
        private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            JComponent renderer = (JComponent) defaultRenderer.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            renderer.setBorder(insetBorder);
            renderer.setEnabled(FlatComboBox.this.isEnabled());
            return renderer;
        }
    }

    static class FlatTextComboBoxEditor implements ComboBoxEditor {
        private final FlatTextField textEditor = new FlatTextField();

        @Override
        public Component getEditorComponent() {
            return textEditor;
        }

        @Override
        public Object getItem() {
            return textEditor.getText();
        }

        @Override
        public void setItem(Object value) {
            if (value instanceof String text) {
                textEditor.setText(text);
            }
        }

        @Override
        public void selectAll() {
            textEditor.selectAll();
        }

        @Override
        public void addActionListener(ActionListener l) {
            textEditor.addActionListener(l);
        }

        @Override
        public void removeActionListener(ActionListener l) {
            textEditor.removeActionListener(l);
        }
    }

    public FlatComboBox() {
        super();
        setUI(new FlatComboBoxUI());
        setRenderer(new FlatListCellRenderer());
        setEditor(new FlatTextComboBoxEditor());
        setFocusable(false);
        setMaximumRowCount(25);

        AbstractButton arrowButton = getArrowButton();
        if (arrowButton != null) {
            arrowButton.setBorderPainted(false);
            arrowButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isOpaque()) {
            Rectangle rect = getValueRectangle();
            g.setColor(PANEL_BACKGROUND);
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

    private AbstractButton getArrowButton() {
        for (Component component : getComponents()) {
            if (component instanceof AbstractButton arrowButton) {
                return arrowButton;
            }
        }
        return null;
    }

}
