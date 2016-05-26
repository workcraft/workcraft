package org.workcraft.plugins.cpog.gui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
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
import org.workcraft.plugins.cpog.tasks.SatBasedSolver;
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

        createStandardPanel();
        createGenerationPanel();
        createButtonPanel();

        double[][] size = new double[][] {{TableLayout.FILL },
                {60, TableLayout.FILL, 39 }, };

        TableLayout layout = new TableLayout(size);
        layout.setHGap(3);
        layout.setVGap(3);

        JPanel content = new JPanel(layout);
        content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        content.add(standardPanel, "0, 0");
        content.add(generationPanel, "0 1");
        content.add(buttonsPanel, "0 2");

        setContentPane(content);

        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        sizeWindow(395, 240, 200, 100);
    }

    private void createStandardPanel() {

        standardPanel = new JPanel(new SimpleFlowLayout());

        // OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
        JLabel optimiseLabel = new JLabel(ScencoDialogSupport.textOptimiseForLabel);
        //optimiseLabel.setPreferredSize(ScencoDialogSupport.dimensionOptimiseForLabel);
        optimiseBox = new JComboBox<String>();
        optimiseBox.setEditable(false);
        optimiseBox.setPreferredSize(ScencoDialogSupport.dimensionOptimiseForBox);
        optimiseBox.addItem(ScencoDialogSupport.textOptimiseForFirstElement);
        optimiseBox.addItem(ScencoDialogSupport.textOptimiseForSecondElement);
        optimiseBox.setSelectedIndex(settings.isCpogSize() ? 0 : 1);
        optimiseBox.setBackground(Color.WHITE);

        // ABC TOOL DISABLE FLAG
        abcCheck = new JCheckBox("", settings.isAbcFlag());
        JLabel abcLabel = new JLabel("Use ABC for logic synthesis");
        //abcLabel.setPreferredSize(ScencoDialogSupport.dimensionShortLabel);
        abcLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                abcCheck.setSelected(abcCheck.isSelected() ? false : true);
            }
        });

        // VERBOSE MODE INSTANTIATION
        JLabel verboseModeLabel = new JLabel(ScencoDialogSupport.textVerboseMode);
        //verboseModeLabel.setPreferredSize(ScencoDialogSupport.dimensionVerboseLabel);
        verboseModeCheck = new JCheckBox("", false);
        verboseModeLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                verboseModeCheck.setSelected(verboseModeCheck.isSelected() ? false
                        : true);
            }
        });

        // ADD EVERYTHING INTO THE PANEL
        standardPanel.add(optimiseLabel);
        standardPanel.add(optimiseBox);
        standardPanel.add(new SimpleFlowLayout.LineBreak());
        standardPanel.add(abcCheck);
        standardPanel.add(abcLabel);
        standardPanel.add(verboseModeCheck);
        standardPanel.add(verboseModeLabel);
        standardPanel.add(new SimpleFlowLayout.LineBreak());
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
                settings.setCustomEncMode(false);

                // Set them on encoder
                SatBasedSolver encoder = new SatBasedSolver(settings);

                // Execute scenco
                encoder.run(we);
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
        setPreferredSize(new Dimension(width, height));
        //setResizable(false);
        pack();

    }
}
