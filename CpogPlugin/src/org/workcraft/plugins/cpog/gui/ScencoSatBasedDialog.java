package org.workcraft.plugins.cpog.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.util.GUI;
import org.workcraft.util.IntDocument;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class ScencoSatBasedDialog extends JDialog {

    private JCheckBox verboseModeCheck, abcCheck;
    private JComboBox<String> optimiseBox;
    private JPanel generationPanel, buttonsPanel, standardPanel;
    private JTextField bitsText, circuitSizeText;
    JScrollPane scrollPane;
    private int m, bits;
    // Core variables
    private final EncoderSettings settings;
    private final WorkspaceEntry we;
    private int modalResult;

    // generationPanel.getPreferredSize().height

    public EncoderSettings getSettings() {
        return settings;
    }

    public ScencoSatBasedDialog(Window owner,
            PresetManager<EncoderSettings> presetManager,
            EncoderSettings settings, WorkspaceEntry we) {
        super(owner, "SAT-based optimal encoding",
                ModalityType.APPLICATION_MODAL);
        this.settings = settings;
        this.we = we;
        modalResult = 0;

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

        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
    }

    private void createStandardPanel() {

        standardPanel = new JPanel();
        standardPanel.setLayout(new BoxLayout(standardPanel, BoxLayout.PAGE_AXIS));

        // OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
        JLabel optimiseLabel = new JLabel(ScencoDialogSupport.textOptimiseForLabel);
        optimiseBox = new JComboBox<String>();
        optimiseBox.setEditable(false);
        optimiseBox.setPreferredSize(ScencoDialogSupport.dimensionOptimiseForBox);
        optimiseBox.addItem(ScencoDialogSupport.textOptimiseForFirstElement);
        optimiseBox.addItem(ScencoDialogSupport.textOptimiseForSecondElement);
        optimiseBox.setSelectedIndex(settings.isCpogSize() ? 0 : 1);
        optimiseBox.setBackground(Color.WHITE);

        JPanel optimisePanel = new JPanel();
        optimisePanel.add(optimiseLabel);
        optimisePanel.add(optimiseBox);

        // ABC TOOL DISABLE FLAG
        abcCheck = new JCheckBox("Use ABC for logic synthesis", settings.isAbcFlag());

        // VERBOSE MODE INSTANTIATION
        verboseModeCheck = new JCheckBox(ScencoDialogSupport.textVerboseMode, false);

        JPanel checkPanel = new JPanel();
        checkPanel.add(abcCheck);
        checkPanel.add(verboseModeCheck);

        // ADD EVERYTHING INTO THE PANEL
        standardPanel.add(optimisePanel);
        standardPanel.add(checkPanel);
    }

    private void createGenerationPanel() {
        VisualCpog cpog = (VisualCpog) (we.getModelEntry().getVisualModel());
        ArrayList<VisualTransformableNode> scenarios = CpogParsingTool.getScenarios(cpog);
        m = scenarios.size();

        generationPanel = new JPanel(new SimpleFlowLayout());
        generationPanel.setBorder(BorderFactory
                .createTitledBorder("Encoding parameters"));

        JLabel bitsLabel = new JLabel(ScencoDialogSupport.textEncodingBitWidth);
        //bitsLabel.setPreferredSize(ScencoDialogSupport.dimensionBitEncodingWidthLabel);
        JLabel circuitSizeLabel = new JLabel(ScencoDialogSupport.textCircuitSizeLabel);
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
        circuitSizeText.setPreferredSize(ScencoDialogSupport.dimensionCircuitSizeText);

        generationPanel.add(bitsLabel);
        generationPanel.add(bitsText);
        generationPanel.add(new SimpleFlowLayout.LineBreak());
        generationPanel.add(circuitSizeLabel);
        generationPanel.add(circuitSizeText);
        generationPanel.add(new SimpleFlowLayout.LineBreak());

    }

    private void createButtonPanel() {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = GUI.createDialogButton("Run");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);

                // ENCODER EXECUTION

                // abc disabled
                settings.setAbcFlag(abcCheck.isSelected() ? true : false);

                // speed-up mode selection
                settings.setEffort(true);
                settings.setCostFunc(false);

                // number of bits selection
                settings.setBits(Integer.parseInt(bitsText.getText()));

                // circuit size selection
                settings.setCircuitSize(Integer.valueOf(circuitSizeText
                        .getText()));

                // optimise for option
                settings.setCpogSize(optimiseBox.getSelectedIndex() == 0 ? false
                        : true);

                // verbose mode
                settings.setVerboseMode(verboseModeCheck.isSelected());

                // continuous mode or number of solutions
                settings.setSolutionNumber(10);

                // generation mode selection
                settings.setGenerationModeInt(3);

                // custom encodings
                settings.setNumPO(m);
                settings.setCustomEncMode(true);

                modalResult = 1;
            }
        });

        JButton closeButton = GUI.createDialogButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        buttonsPanel.add(saveButton);
        buttonsPanel.add(closeButton);

    }

    private void sizeWindow(int width, int height, int row1, int row2) {
        setMinimumSize(new Dimension(width, height));
        pack();
    }

    public int getModalResult() {
        return modalResult;
    }

}
