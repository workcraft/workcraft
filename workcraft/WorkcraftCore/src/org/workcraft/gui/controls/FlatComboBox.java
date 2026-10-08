package org.workcraft.gui.controls;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.function.BiPredicate;

public class FlatComboBox extends JComboBox<Object> {

    // Marker item that is rendered as a separator line (with optional text) and cannot be selected
    private record Separator(String text) {
    }

    private static final Border INSET_BORDER = GuiUtils.getTableCellBorder();
    private static final String COMMIT_ACTION = "commitEditorText";

    // Text in editor was typed by user after the last programmatic update or list navigation
    private boolean textTyped = false;

    private BiPredicate<Object, Object> currentItemMatcher = null;

    static class FlatComboBoxUI extends BasicComboBoxUI {

        AbstractButton getArrowButton() {
            return arrowButton;
        }

        JList<Object> getPopupList() {
            return popup.getList();
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
            // As a table cell editor, arrow keys move the popup list selection (not the combo box one)
            popup.getList().setSelectionModel(new DefaultListSelectionModel() {
                @Override
                public void setSelectionInterval(int index0, int index1) {
                    if ((index0 == index1) && (index0 >= 0) && (index0 < comboBox.getItemCount())
                            && (comboBox.getItemAt(index0) instanceof Separator)) {

                        // Mouse hover over separator keeps the current selection. Keys step over it
                        // in the direction of movement.
                        if (EventQueue.getCurrentEvent() instanceof KeyEvent) {
                            int next = (index0 >= getMinSelectionIndex()) ? index0 + 1 : index0 - 1;
                            if ((next >= 0) && (next < comboBox.getItemCount())) {
                                super.setSelectionInterval(next, next);
                            }
                        }
                    } else {
                        super.setSelectionInterval(index0, index1);
                    }
                }
            });
            // Navigation of the list by keys supersedes the typed text
            popup.getList().addListSelectionListener(e -> {
                if ((EventQueue.getCurrentEvent() instanceof KeyEvent) && (comboBox instanceof FlatComboBox flat)) {
                    flat.textTyped = false;
                }
            });
            return popup;
        }
    }

    class FlatListCellRenderer extends DefaultListCellRenderer {
        private final JPanel separatorPanel = new JPanel(new BorderLayout());
        private final JLabel separatorLabel = new JLabel();

        // Item is the current value of the combo box (as opposed to the item highlighted in the list)
        private boolean current = false;
        private int checkWidth = 0;
        private Border listItemBorder = INSET_BORDER;

        FlatListCellRenderer() {
            separatorPanel.setBorder(new EmptyBorder(2, 0, 2, 0));
            // Separator draws its line at the top of its area, so centre it vertically within the row
            JPanel lineWrapper = new JPanel(new GridBagLayout());
            lineWrapper.setOpaque(false);
            lineWrapper.add(new JSeparator(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            separatorPanel.add(lineWrapper, BorderLayout.CENTER);
            separatorLabel.setBorder(new EmptyBorder(0, 4, 0, 4));
            separatorPanel.add(separatorLabel, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (current && (g instanceof Graphics2D g2)) {
                paintCheckMark(g2);
            }
        }

        private void paintCheckMark(Graphics2D g2) {
            // Check mark is in the space reserved at the right edge, after the insets
            int size = Math.max(checkWidth / 4, 2);
            int x = getWidth() - getInsets().right + checkWidth / 2;
            int y = getHeight() / 2;
            Stroke stroke = g2.getStroke();
            Object antialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getForeground());
            g2.setStroke(new BasicStroke(Math.max(checkWidth / 8f, 1f)));
            g2.drawPolyline(
                    new int[]{x - size, x - size / 3, x + size},
                    new int[]{y, y + size * 2 / 3, y - size * 2 / 3},
                    3);
            g2.setStroke(stroke);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            if (value instanceof Separator(String text)) {
                separatorPanel.setBackground(list.getBackground());
                boolean hasText = (text != null) && !text.isEmpty();
                separatorLabel.setVisible(hasText);
                if (hasText) {
                    Font font = list.getFont();
                    separatorLabel.setFont(font.deriveFont(font.getSize2D() * 0.8f));
                    separatorLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
                    separatorLabel.setText(text);
                }
                return separatorPanel;
            }

            JComponent renderer = (JComponent) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            // Index is negative when the renderer is used for the value displayed in the combo box itself
            Object selected = getSelectedItem();
            current = (index >= 0) && (selected != null) && isCurrentItem(value, selected);
            if (index >= 0) {
                // Space for the check mark is reserved in all items of the list
                int width = renderer.getFontMetrics(renderer.getFont()).getHeight();
                if ((width != checkWidth) || (listItemBorder == INSET_BORDER)) {
                    checkWidth = width;
                    Insets insets = INSET_BORDER.getBorderInsets(renderer);
                    listItemBorder = new EmptyBorder(insets.top, insets.left, insets.bottom, insets.right + width);
                }
                renderer.setBorder(listItemBorder);
            } else {
                renderer.setBorder(INSET_BORDER);
            }
            renderer.setEnabled(FlatComboBox.this.isEnabled());
            return renderer;
        }
    }

    class FlatTextComboBoxEditor implements ComboBoxEditor {
        private final FlatTextField textEditor = new FlatTextField();
        private boolean updating = false;

        FlatTextComboBoxEditor() {
            // Track text typed by user (as opposed to text set programmatically)
            textEditor.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    textTyped |= !updating;
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    textTyped |= !updating;
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            });
            // Enter commits the typed text even if the drop-down list is open
            textEditor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), COMMIT_ACTION);
            textEditor.getActionMap().put(COMMIT_ACTION, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    commitEditorText();
                }
            });
        }

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
            updating = true;
            try {
                textEditor.setText(value instanceof String text ? text : "");
            } finally {
                updating = false;
            }
            textTyped = false;
        }

        void commit() {
            textEditor.postActionEvent();
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
        setMaximumRowCount(25);
    }

    // Apply text typed by user (if any), e.g. when editing is finished by other means than Enter
    public void commitTypedText() {
        if (textTyped && (getEditor() instanceof FlatTextComboBoxEditor flatEditor)) {
            setPopupVisible(false);
            flatEditor.commit();
        }
    }

    // Custom check whether a list item corresponds to the selected item (e.g. when selected item is a text
    // that stands for a more complex item). By default, the items are compared for equality.
    public void setCurrentItemMatcher(BiPredicate<Object, Object> matcher) {
        currentItemMatcher = matcher;
    }

    private boolean isCurrentItem(Object item, Object selected) {
        return (item != null) && (item.equals(selected)
                || ((currentItemMatcher != null) && currentItemMatcher.test(item, selected)));
    }

    private void commitEditorText() {
        if (isPopupVisible()) {
            // Without typing, Enter picks the list item highlighted by navigation
            if (!textTyped && (getUI() instanceof FlatComboBoxUI flatComboBoxUI)) {
                Object item = flatComboBoxUI.getPopupList().getSelectedValue();
                if ((item != null) && !(item instanceof Separator)) {
                    // Select the item itself, as editor text field cannot represent non-text items
                    boolean changed = !item.equals(getSelectedItem());
                    setPopupVisible(false);
                    setSelectedItem(item);
                    if (!changed) {
                        fireActionEvent();
                    }
                    return;
                }
            }
            setPopupVisible(false);
        }
        if (getEditor() instanceof FlatTextComboBoxEditor flatEditor) {
            flatEditor.commit();
        }
    }

    public void addSeparator() {
        addSeparator(null);
    }

    // Separator with a text shown in small font at its right side
    public void addSeparator(String text) {
        addItem(new Separator(text));
    }

    @Override
    public void setSelectedItem(Object item) {
        if (!(item instanceof Separator)) {
            super.setSelectedItem(item);
        }
    }

    @Override
    public void setSelectedIndex(int index) {
        if ((index >= 0) && (index < getItemCount()) && (getItemAt(index) instanceof Separator)) {
            // Step over separator in the direction of movement (e.g. by arrow keys)
            int next = (index >= getSelectedIndex()) ? index + 1 : index - 1;
            if ((next >= 0) && (next < getItemCount())) {
                super.setSelectedIndex(next);
            }
        } else {
            super.setSelectedIndex(index);
        }
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
