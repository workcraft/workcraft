package org.workcraft.plugins.circuit.tools;

import info.clearthought.layout.TableLayout;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class ScanDialog extends JDialog {

    private boolean modalResult;

    public ScanDialog(Window owner) {
        super(owner, "Scan insertion", ModalityType.DOCUMENT_MODAL);

        JPanel outputOptions = new JPanel();
        outputOptions.setLayout(new BoxLayout(outputOptions, BoxLayout.Y_AXIS));
        outputOptions.setBorder(SizeHelper.getTitledBorder("Shared signals"));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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

        int buttonPanelHeight = buttonsPanel.getPreferredSize().height;
        double[][] sizes = new double[][] {
                {TableLayout.FILL},
                {TableLayout.PREFERRED, TableLayout.FILL, buttonPanelHeight},
        };
        final JPanel contentPanel = new JPanel(new TableLayout(sizes));
        contentPanel.setBorder(SizeHelper.getEmptyBorder());

        contentPanel.add(buttonsPanel, "0 2");

        setContentPane(contentPanel);
        setMinimumSize(new Dimension(600, 400));
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
