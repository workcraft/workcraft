package org.workcraft.gui.controls;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionListener;

public class FlatComboBox extends JComboBox<Object> {

    private static final Border INSET_BORDER = GuiUtils.getTableCellBorder();

    static class FlatComboBoxUI extends BasicComboBoxUI {

        AbstractButton getArrowButton() {
            return arrowButton;
        }

        @Override
        protected void configureEditor() {
            super.configureEditor();
            // Combo box is not focusable, but its editor needs focus to accept text input
            editor.setFocusable(true);
        }

        @Override
        protected JButton createArrowButton() {
            // Default arrow button paints a 3D bevel on hover and when pressed
            BasicArrowButton button = new BasicArrowButton(SwingConstants.SOUTH,
                    UIManager.getColor("ComboBox.buttonBackground"),
                    UIManager.getColor("ComboBox.buttonShadow"),
                    UIManager.getColor("ComboBox.buttonDarkShadow"),
                    UIManager.getColor("ComboBox.buttonHighlight")) {

                @Override
                public Color getBackground() {
                    // Hover and pressed feedback (super.paint() also uses this colour)
                    Color color = super.getBackground();
                    ButtonModel model = getModel();
                    if ((color == null) || (model == null)) {
                        return color;
                    }
                    if (model.isPressed()) {
                        return color.darker();
                    }
                    if (model.isRollover()) {
                        return new Color(color.getRed() * 9 / 10, color.getGreen() * 9 / 10, color.getBlue() * 9 / 10);
                    }
                    return color;
                }

                @Override
                public void paint(Graphics g) {
                    // Super paints only the inner area, leaving a 1px ring unfilled
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    super.paint(g);
                }
            };
            button.setName("ComboBox.arrowButton");
            button.setRolloverEnabled(true);
            // Non-UIResource border makes super.paint() skip the 3D bevel
            button.setBorder(new EmptyBorder(0, 0, 0, 0));
            return button;
        }

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

    class FlatListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            JComponent renderer = (JComponent) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            renderer.setBorder(INSET_BORDER);
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
            textEditor.setText(value instanceof String text ? text : "");
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
        setRenderer(new FlatListCellRenderer());
        setEditor(new FlatTextComboBoxEditor());
        setFocusable(false);
        setMaximumRowCount(25);
    }

    @Override
    public void updateUI() {
        // Keep flat UI after look-and-feel change
        setUI(new FlatComboBoxUI());
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isOpaque()) {
            Rectangle rect = getValueRectangle();
            g.setColor(UIManager.getColor("Panel.background"));
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
    }

    public Rectangle getValueRectangle() {
        Dimension d = getSize();
        // Value area ends where the arrow button starts, whatever the border insets are
        if ((getUI() instanceof FlatComboBoxUI flatComboBoxUI) && (flatComboBoxUI.getArrowButton() != null)) {
            d.width = flatComboBoxUI.getArrowButton().getX();
        }
        return new Rectangle(d);
    }

}
