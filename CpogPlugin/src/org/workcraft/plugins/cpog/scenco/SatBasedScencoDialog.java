package org.workcraft.plugins.cpog.scenco;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.layouts.SimpleFlowLayout;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.utils.GuiUtils;
import org.workcraft.shared.IntDocument;

import info.clearthought.layout.TableLayout;

@SuppressWarnings("serial")
public class SatBasedScencoDialog extends AbstractScencoDialog {

    private JCheckBox verboseModeCheck, abcCheck;
    private JComboBox<String> optimiseBox;
    private JPanel generationPanel, buttonsPanel, standardPanel;
    private JTextField bitsText, circuitSizeText;
    JScrollPane scrollPane;
    private int m, bits;

    public SatBasedScencoDialog(Window owner, String title, EncoderSettings settings, VisualCpog model) {
        super(owner, title, settings, model);

        createStandardPanel();
        createGenerationPanel();
        createButtonPanel();

        double[][] size = new double[][] {
            {TableLayout.FILL},
            {60, TableLayout.FILL, 39},
        };

        TableLayout layout = new TableLayout(size);
        layout.setHGap(3);
        layout.setVGap(3);

        JPanel content = new JPanel(new BorderLayout());

        content.add(standardPanel, BorderLayout.NORTH);
        content.add(generationPanel);
        content.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(content);

        getRootPane().registerKeyboardAction(e -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
    }

    private void createStandardPanel() {

        standardPanel = new JPanel();
        standardPanel.setLayout(new BoxLayout(standardPanel, BoxLayout.PAGE_AXIS));

        // OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
        JLabel optimiseLabel = new JLabel(ScencoHelper.textOptimiseForLabel);
        optimiseBox = new JComboBox<String>();
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
        ArrayList<VisualTransformableNode> scenarios = CpogParsingTool.getScenarios(getModel());
        m = scenarios.size();

        generationPanel = new JPanel(new SimpleFlowLayout());
        generationPanel.setBorder(SizeHelper.getTitledBorder("Encoding parameters"));

        JLabel bitsLabel = new JLabel(ScencoHelper.textEncodingBitWidth);
        //bitsLabel.setPreferredSize(ScencoDialogSupport.dimensionBitEncodingWidthLabel);
        JLabel circuitSizeLabel = new JLabel(ScencoHelper.textCircuitSizeLabel);
        //circuitSizeLabel.setPreferredSize(ScencoDialogSupport.dimensionCircuitSizeLabel);
        int value = 2;
        while (value < m) {
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
        runButton.addActionListener(event -> actionRun());

        JButton closeButton = GuiUtils.createDialogButton("Close");
        closeButton.addActionListener(e -> setVisible(false));

        buttonsPanel.add(runButton);
        buttonsPanel.add(closeButton);
    }

    private void actionRun() {
        setVisible(false);

        // ENCODER EXECUTION
        EncoderSettings settings = getSettings();

        // abc disabled
        settings.setAbcFlag(abcCheck.isSelected() ? true : false);

        // speed-up mode selection
        settings.setEffort(true);
        settings.setCostFunc(false);

        // number of bits selection
        settings.setBits(Integer.parseInt(bitsText.getText()));

        // circuit size selection
        settings.setCircuitSize(Integer.valueOf(circuitSizeText.getText()));

        // optimise for option
        settings.setCpogSize(optimiseBox.getSelectedIndex() == 0 ? false : true);

        // verbose mode
        settings.setVerboseMode(verboseModeCheck.isSelected());

        // continuous mode or number of solutions
        settings.setSolutionNumber(10);

        // generation mode selection
        settings.setGenerationModeInt(3);

        // custom encodings
        settings.setNumPO(m);
        settings.setCustomEncMode(true);
        setDone();
    }

}
