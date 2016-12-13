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
import javax.swing.KeyStroke;

import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class ScencoSingleSequentialDialog extends JDialog {

    private JCheckBox verboseModeCheck, abcCheck;
    private JComboBox<String> optimiseBox;
    private JPanel buttonsPanel, standardPanel;
    JScrollPane scrollPane;
    private int m, bits;
    private final EncoderSettings settings;
    private final WorkspaceEntry we;
    private int modalResult;

    public ScencoSingleSequentialDialog(Window owner, PresetManager<EncoderSettings> presetManager, EncoderSettings settings, WorkspaceEntry we, String string) {
        super(owner, string, ModalityType.APPLICATION_MODAL);
        this.settings = settings;
        this.we = we;
        modalResult = 0;

        createStandardPanel();
        createButtonPanel(string);

        double[][] size = new double[][] {
            {TableLayout.FILL},
            {60, TableLayout.FILL},
        };

        TableLayout layout = new TableLayout(size);
        layout.setHGap(3);
        layout.setVGap(3);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        content.add(standardPanel);
        content.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(content);

        getRootPane().registerKeyboardAction(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setVisible(false);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        //sizeWindow(365, 151, 200, 100);
        pack();
    }

    private void createStandardPanel() {

        standardPanel = new JPanel();

        // OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
        JPanel optimisePanel = new JPanel();
        JLabel optimiseLabel = new JLabel(ScencoDialogSupport.textOptimiseForLabel);
        //optimiseLabel.setPreferredSize(ScencoDialogSupport.dimensionOptimiseForLabel);
        optimiseBox = new JComboBox<String>();
        optimiseBox.setEditable(false);
        optimiseBox.setPreferredSize(ScencoDialogSupport.dimensionOptimiseForBox);
        optimiseBox.addItem(ScencoDialogSupport.textOptimiseForFirstElement);
        optimiseBox.addItem(ScencoDialogSupport.textOptimiseForSecondElement);
        optimiseBox.setSelectedIndex(settings.isCpogSize() ? 0 : 1);
        optimiseBox.setBackground(Color.WHITE);
        optimisePanel.add(optimiseLabel);
        optimisePanel.add(optimiseBox);

        // ABC TOOL DISABLE FLAG
        abcCheck = new JCheckBox(ScencoDialogSupport.textAbcLabel, settings.isAbcFlag());

        // VERBOSE MODE INSTANTIATION
        verboseModeCheck = new JCheckBox(ScencoDialogSupport.textVerboseMode, false);

        JPanel checkPanel = new JPanel();
        checkPanel.add(abcCheck);
        checkPanel.add(verboseModeCheck);

        standardPanel.setLayout(new BoxLayout(standardPanel, BoxLayout.PAGE_AXIS));

        standardPanel.add(optimisePanel);
        standardPanel.add(checkPanel);
    }

    private void createButtonPanel(final String string) {
        VisualCpog cpog = (VisualCpog) (we.getModelEntry().getVisualModel());
        ArrayList<VisualTransformableNode> scenarios = CpogParsingTool.getScenarios(cpog);
        m = scenarios.size();

        int value = 2;
        while (value < m) {
            value *= 2;
            bits++;
        }

        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = GUI.createDialogButton("Run");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);

                // abc disabled
                settings.setAbcFlag(abcCheck.isSelected() ? true : false);

                // number of bits selection
                settings.setBits(bits + 1);

                // optimise for option
                settings.setCpogSize(optimiseBox.getSelectedIndex() == 0 ? false : true);

                // verbose mode
                settings.setVerboseMode(verboseModeCheck.isSelected());

                // generation mode selection
                settings.setGenerationModeInt(string.matches("Single-literal encoding") ? 4 : 5);

                // custom encodings
                settings.setNumPO(m);
                if (settings.getGenMode() == GenerationMode.SEQUENTIAL) {
                    settings.setCustomEncMode(true);
                    String[] encodings = new String[m];
                    for (int i = 0; i < m; i++) {
                        encodings[i] = Integer.toBinaryString(i);
                    }
                    settings.setCustomEnc(encodings);
                } else {
                    settings.setBits(bits + 1);
                    settings.setCustomEncMode(false);
                }

                // Execute scenco
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

    public void setModalResult(int modalResult) {
        this.modalResult = modalResult;
    }

    public EncoderSettings getSettings() {
        return settings;
    }
}
