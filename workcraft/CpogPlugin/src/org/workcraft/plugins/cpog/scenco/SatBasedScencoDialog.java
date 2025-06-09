package org.workcraft.plugins.cpog.scenco;

import org.workcraft.gui.layouts.SimpleFlowLayout;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.shared.IntDocument;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class SatBasedScencoDialog extends AbstractScencoDialog {

    private JCheckBox verboseModeCheck;
    private JCheckBox abcCheck;
    private JComboBox<String> optimiseBox;
    private JPanel generationPanel;
    private JPanel buttonsPanel;
    private JPanel standardPanel;
    private JTextField bitsText;
    private JTextField circuitSizeText;
    private int bits;

    public SatBasedScencoDialog(Window owner, String title, EncoderSettings settings, VisualCpog model) {
        super(owner, title, settings, model);

        createStandardPanel();
        createGenerationPanel();
        createButtonPanel();

        JPanel content = new JPanel(new BorderLayout());

        content.add(standardPanel, BorderLayout.NORTH);
        content.add(generationPanel);
        content.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(content);

        getRootPane().registerKeyboardAction(e -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(owner);
    }

    private void createStandardPanel() {

        standardPanel = new JPanel();
        standardPanel.setLayout(new BoxLayout(standardPanel, BoxLayout.PAGE_AXIS));

        // OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
        JLabel optimiseLabel = new JLabel(ScencoHelper.textOptimiseForLabel);
        optimiseBox = new JComboBox<>();
        optimiseBox.setEditable(false);
        optimiseBox.setPreferredSize(ScencoHelper.dimensionOptimiseForBox);
        optimiseBox.addItem(ScencoHelper.textOptimiseForFirstElement);
        optimiseBox.addItem(ScencoHelper.textOptimiseForSecondElement);
        optimiseBox.setSelectedIndex(getSettings().isCpogSize() ? 0 : 1);
        optimiseBox.setBackground(Color.WHITE);

        JPanel optimisePanel = new JPanel();
        optimisePanel.add(optimiseLabel);
        optimisePanel.add(optimiseBox);

        // ABC TOOL DISABLE FLAG
        abcCheck = new JCheckBox("Use ABC for logic synthesis", getSettings().isAbcFlag());

        // VERBOSE MODE INSTANTIATION
        verboseModeCheck = new JCheckBox(ScencoHelper.textVerboseMode, false);

        JPanel checkPanel = new JPanel();
        checkPanel.add(abcCheck);
        checkPanel.add(verboseModeCheck);

        // ADD EVERYTHING INTO THE PANEL
        standardPanel.add(optimisePanel);
        standardPanel.add(checkPanel);
    }

    private void createGenerationPanel() {
        generationPanel = new JPanel(new SimpleFlowLayout());
        generationPanel.setBorder(GuiUtils.getTitledBorder("Encoding parameters"));

        JLabel bitsLabel = new JLabel(ScencoHelper.textEncodingBitWidth);
        JLabel circuitSizeLabel = new JLabel(ScencoHelper.textCircuitSizeLabel);
        int value = 2;
        while (value < CpogParsingTool.getScenarios(getModel()).size()) {
            value *= 2;
            bits++;
        }

        bitsText = new JTextField();
        bitsText.setDocument(new IntDocument(2));
        bitsText.setText(String.valueOf(bits + 1));
        bitsText.setPreferredSize(new Dimension(35, 20));
        bitsText.setBackground(Color.WHITE);
        bitsText.setEnabled(true);

        circuitSizeText = new JTextField();
        circuitSizeText.setText(String.valueOf(bits + 2));
        circuitSizeText.setPreferredSize(ScencoHelper.dimensionCircuitSizeText);

        generationPanel.add(bitsLabel);
        generationPanel.add(bitsText);
        generationPanel.add(new SimpleFlowLayout.LineBreak());
        generationPanel.add(circuitSizeLabel);
        generationPanel.add(circuitSizeText);
        generationPanel.add(new SimpleFlowLayout.LineBreak());

    }

    private void createButtonPanel() {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton runButton = GuiUtils.createDialogButton("Run");
        runButton.addActionListener(event -> runAction());

        JButton closeButton = GuiUtils.createDialogButton("Close");
        closeButton.addActionListener(e -> setVisible(false));

        buttonsPanel.add(runButton);
        buttonsPanel.add(closeButton);
    }

    private void runAction() {
        setVisible(false);

        // ENCODER EXECUTION
        EncoderSettings settings = getSettings();

        // abc disabled
        settings.setAbcFlag(abcCheck.isSelected());

        // speed-up mode selection
        settings.setEffort(true);
        settings.setCostFunc(false);

        // number of bits selection
        settings.setBits(Integer.parseInt(bitsText.getText()));

        // circuit size selection
        settings.setCircuitSize(Integer.parseInt(circuitSizeText.getText()));

        // optimise for option
        settings.setCpogSize(optimiseBox.getSelectedIndex() != 0);

        // verbose mode
        settings.setVerboseMode(verboseModeCheck.isSelected());

        // continuous mode or number of solutions
        settings.setSolutionNumber(10);

        // generation mode selection
        settings.setGenerationModeInt(3);

        // custom encodings
        settings.setCustomEncMode(true);
    }

}
