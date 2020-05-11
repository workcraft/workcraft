package org.workcraft.gui.dialogs;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class ModalDialog<T> extends JDialog {

    private final T userData;
    private JButton okButton;
    private boolean modalResult;
    private final JPanel actionPanel;
    private final JPanel buttonsPanel;


    public ModalDialog(Window owner, String title, T userData) {
        super(owner, title, ModalityType.DOCUMENT_MODAL);
        this.userData = userData;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(event -> okAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(event -> cancelAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        buttonsPanel = createButtonsPanel();
        actionPanel = createActionPanel();
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(actionPanel, BorderLayout.WEST);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        panel.setBorder(GuiUtils.getEmptyBorder());
        panel.add(createContentPanel(), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(panel);

        pack();
        Dimension dimension = getSize();
        setMinimumSize(SizeHelper.getFitScreenDimension(dimension, 0.3f, 0.3f));
        setSize(SizeHelper.getFitScreenDimension(dimension, 0.5f, 0.5f));
        setLocationRelativeTo(owner);
    }

    public JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(GuiUtils.getContentPanelBorder());
        return panel;
    }

    public JPanel createActionPanel() {
        return GuiUtils.createDialogActionPanel();
    }

    public JPanel getActionPanel() {
        return actionPanel;
    }

    public JButton addAction(String text, ActionListener action) {
        JButton button = GuiUtils.createDialogButton(text);
        button.addActionListener(action);
        getActionPanel().add(button);
        return button;
    }

    public JPanel createButtonsPanel() {
        JPanel buttonsPanel = GuiUtils.createDialogButtonsPanel();
        okButton = GuiUtils.createDialogButton("OK");
        okButton.addActionListener(event -> okAction());

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> cancelAction());

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        return buttonsPanel;
    }

    public JPanel getButtonsPanel() {
        return buttonsPanel;
    }

    public JButton addButton(String text, ActionListener action) {
        JButton button = GuiUtils.createDialogButton(text);
        button.addActionListener(action);
        getButtonsPanel().add(button);
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
