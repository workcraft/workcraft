package org.workcraft.plugins.cpog.gui;

import org.workcraft.gui.layouts.SimpleFlowLayout;
import org.workcraft.plugins.cpog.commands.PetriToCpogParameters;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PetriToCpogDialog extends JDialog {

    // check boxes
    private JCheckBox reduceCheck;
    private JCheckBox isomorphismCheck;
    private JCheckBox removeNodesCheck;

    //other elements
    private JComboBox<String> significanceBox;
    private JPanel buttonPanel;
    private JPanel settingPanel;
    private boolean modalResult;

    public PetriToCpogDialog(Window owner, PetriToCpogParameters parameters) {
        super(owner, "Petri Net to CPOG conversion [Untanglings]", ModalityType.APPLICATION_MODAL);

        createSettingPanel();
        createButtonPanel(parameters);

        JPanel content = new JPanel(GuiUtils.createBorderLayout());
        content.setBorder(GuiUtils.getEmptyBorder());

        content.add(settingPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(content);

        getRootPane().registerKeyboardAction(event -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setMinimumSize(new Dimension(560, 200));
        setLocationRelativeTo(owner);
    }

    /** creates the panel containing the settings of the converter **/
    private void createSettingPanel() {
        settingPanel = new JPanel(new SimpleFlowLayout());

        // reduction of maximal significant runs, check box
        reduceCheck = new JCheckBox("", true);
        JLabel reduceLabel = new JLabel(PetriToCpogDialogSupport.textReduceLabel);
        reduceLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                reduceCheck.setSelected(!reduceCheck.isSelected());
            }
        });

        // reduce isomorphic processes, check box
        isomorphismCheck = new JCheckBox("", true);
        JLabel isomorphismLabel = new JLabel(PetriToCpogDialogSupport.textIsomorphismLabel);
        isomorphismLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isomorphismCheck.setSelected(!isomorphismCheck.isSelected());
            }
        });

        // algorithm for checking significance property of a run, combo box
        JLabel significanceLabel = new JLabel(PetriToCpogDialogSupport.textSignificanceLabel);
        significanceBox = new JComboBox<>();
        significanceBox.setEditable(false);
        significanceBox.setPreferredSize(PetriToCpogDialogSupport.significanceSize);
        significanceBox.addItem(PetriToCpogDialogSupport.significanceItems[0]);
        significanceBox.addItem(PetriToCpogDialogSupport.significanceItems[1]);
        significanceBox.addItem(PetriToCpogDialogSupport.significanceItems[2]);
        significanceBox.setSelectedIndex(0);
        significanceBox.setBackground(Color.WHITE);

        // remove condition nodes check box
        removeNodesCheck = new JCheckBox("", false);
        JLabel removeNodesLabel = new JLabel(PetriToCpogDialogSupport.textRemoveNodeLabel);
        removeNodesLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeNodesCheck.setSelected(removeNodesCheck.isSelected() ? false : true);
            }
        });

        // adding everything into the panel
        settingPanel.add(significanceLabel);
        settingPanel.add(significanceBox);
        settingPanel.add(new SimpleFlowLayout.LineBreak());
        settingPanel.add(reduceCheck);
        settingPanel.add(reduceLabel);
        settingPanel.add(new SimpleFlowLayout.LineBreak());
        settingPanel.add(isomorphismCheck);
        settingPanel.add(isomorphismLabel);
        settingPanel.add(new SimpleFlowLayout.LineBreak());
        settingPanel.add(removeNodesCheck);
        settingPanel.add(removeNodesLabel);

    }

    /** creates panel containing the buttons for running or closing the converter **/
    private void createButtonPanel(final PetriToCpogParameters parameters) {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // run the converter
        JButton runButton = GuiUtils.createDialogButton("Run");
        runButton.addActionListener(event -> runAction(parameters));

        // close the converter
        JButton closeButton = GuiUtils.createDialogButton("Close");
        closeButton.addActionListener(event -> closeAction());
        buttonPanel.add(runButton);
        buttonPanel.add(closeButton);
    }

    private void runAction(PetriToCpogParameters parameters) {
        parameters.setReduce(reduceCheck.isSelected());
        parameters.setIsomorphism(isomorphismCheck.isSelected());
        parameters.setSignificance(significanceBox.getSelectedIndex());
        parameters.setRemoveNodes(removeNodesCheck.isSelected());
        modalResult = true;
        setVisible(false);
    }

    private void closeAction() {
        modalResult = false;
        setVisible(false);
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}
