package org.workcraft.gui.dialogs;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class ModalDialog extends JDialog {

    private boolean modalResult;

    public ModalDialog(Window owner, String title) {
        super(owner, title, ModalityType.DOCUMENT_MODAL);

        getRootPane().registerKeyboardAction(event -> actionOk(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(event -> actionCancel(),
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

    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        JButton okButton = GuiUtils.createDialogButton("OK");
        okButton.addActionListener(event -> actionOk());

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> actionCancel());

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        return buttonsPanel;
    }

    private void actionOk() {
        modalResult = true;
        setVisible(false);
    }

    private void actionCancel() {
        modalResult = false;
        setVisible(false);
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}
