package org.workcraft.gui.dialogs;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ModalDialog<T> extends JDialog {

    private final T userData;
    private JButton okButton;
    private boolean modalResult;

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

        JPanel contentPanel = new JPanel(new BorderLayout(SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        contentPanel.setBorder(SizeHelper.getEmptyBorder());
        contentPanel.add(createControlsPanel(), BorderLayout.CENTER);
        contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);
        setContentPane(contentPanel);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    public JPanel createControlsPanel() {
        return new JPanel();
    }

    public JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        okButton = GuiUtils.createDialogButton("OK");
        okButton.addActionListener(event -> okAction());

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> cancelAction());

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        return buttonsPanel;
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
