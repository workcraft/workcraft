package org.workcraft.gui.dialogs;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class ModalDialog<T> extends JDialog {

    private final T userData;
    private final JButton okButton;
    private final JPanel leftButtonsPanel;
    private final JPanel rightButtonsPanel;
    private boolean modalResult;

    public ModalDialog(Window owner, String title, T userData) {
        super(owner, title, ModalityType.DOCUMENT_MODAL);
        this.userData = userData;

        leftButtonsPanel = GuiUtils.createDialogButtonsPanel();
        rightButtonsPanel = GuiUtils.createDialogButtonsPanel();
        okButton = addButton("OK", event -> okAction(), true);
        addButton("Cancel", event -> cancelAction(), true);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(leftButtonsPanel, BorderLayout.WEST);
        bottomPanel.add(rightButtonsPanel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        panel.setBorder(GuiUtils.getEmptyBorder());
        panel.add(createContentPanel(), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(panel);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(event -> okAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(event -> cancelAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        GuiUtils.reduceToScreen(this, 0.5f, 0.5f);
        setLocationRelativeTo(owner);
    }

    public JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(GuiUtils.getContentPanelBorder());
        return panel;
    }

    public JButton addButton(String text, ActionListener action, boolean rightAlignment) {
        JButton button = GuiUtils.createDialogButton(text);
        button.addActionListener(action);
        if (rightAlignment) {
            rightButtonsPanel.add(button);
        } else {
            leftButtonsPanel.add(button);
        }
        return button;
    }

    public T getUserData() {
        return userData;
    }

    public void setOkEnableness(boolean value) {
        okButton.setEnabled(value);
    }

    public boolean okAction() {
        if (okButton.isEnabled()) {
            modalResult = true;
            setVisible(false);
            return true;
        }
        return false;
    }

    public void cancelAction() {
        modalResult = false;
        setVisible(false);
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}
