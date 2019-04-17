package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class ScanDialog extends JDialog {

    private boolean modalResult;

    public ScanDialog(Window owner) {
        super(owner, "Scan insertion", ModalityType.DOCUMENT_MODAL);

        JPanel inputPanel = new JPanel(new BorderLayout());

        JTextField portsText = new JTextField(CircuitSettings.getScanPorts());
        inputPanel.add(GuiUtils.createWideLabeledComponent(portsText, "Scan ports: "), BorderLayout.NORTH);

        JTextField pinsText = new JTextField(CircuitSettings.getScanPins());
        inputPanel.add(GuiUtils.createWideLabeledComponent(pinsText, "Scan pins: "), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        JButton okButton = GuiUtils.createDialogButton("OK");
        okButton.addActionListener(event -> actionOk());

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> actionCancel());

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        getRootPane().registerKeyboardAction(event -> actionOk(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(event -> actionCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(inputPanel, BorderLayout.CENTER);
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);
        setMinimumSize(new Dimension(600, 200));
        pack();
        setLocationRelativeTo(owner);
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
