package org.workcraft.plugins.cpog.scenco;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.KeyStroke;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.util.GUI;

import info.clearthought.layout.TableLayout;

@SuppressWarnings("serial")
public class SingleSequentialScencoDialog extends AbstractScencoDialog {

    private JCheckBox verboseModeCheck, abcCheck;
    private JComboBox<String> optimiseBox;
    private JPanel buttonsPanel, standardPanel;
    JScrollPane scrollPane;
    private int m, bits;

    public SingleSequentialScencoDialog(Window owner, String title, EncoderSettings settings, VisualCpog model) {
        super(owner, title, settings, model);

        createStandardPanel();
        createButtonPanel(title);

        double[][] size = new double[][] {
            {TableLayout.FILL},
            {60, TableLayout.FILL},
        };

        TableLayout layout = new TableLayout(size);
        layout.setHGap(SizeHelper.getLayoutHGap());
        layout.setVGap(SizeHelper.getLayoutVGap());

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(SizeHelper.getEmptyBorder());

        content.add(standardPanel);
        content.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(content);

        getRootPane().registerKeyboardAction(event -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        //sizeWindow(365, 151, 200, 100);
        pack();
    }

    private void createStandardPanel() {

        standardPanel = new JPanel();

        // OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
        JPanel optimisePanel = new JPanel();
        JLabel optimiseLabel = new JLabel(ScencoHelper.textOptimiseForLabel);
        //optimiseLabel.setPreferredSize(ScencoDialogSupport.dimensionOptimiseForLabel);
        optimiseBox = new JComboBox<String>();
        optimiseBox.setEditable(false);
        optimiseBox.setPreferredSize(ScencoHelper.dimensionOptimiseForBox);
        optimiseBox.addItem(ScencoHelper.textOptimiseForFirstElement);
        optimiseBox.addItem(ScencoHelper.textOptimiseForSecondElement);
        optimiseBox.setSelectedIndex(getSettings().isCpogSize() ? 0 : 1);
        optimiseBox.setBackground(Color.WHITE);
        optimisePanel.add(optimiseLabel);
        optimisePanel.add(optimiseBox);

        // ABC TOOL DISABLE FLAG
        abcCheck = new JCheckBox(ScencoHelper.textAbcLabel, getSettings().isAbcFlag());

        // VERBOSE MODE INSTANTIATION
        verboseModeCheck = new JCheckBox(ScencoHelper.textVerboseMode, false);

        JPanel checkPanel = new JPanel();
        checkPanel.add(abcCheck);
        checkPanel.add(verboseModeCheck);

        standardPanel.setLayout(new BoxLayout(standardPanel, BoxLayout.PAGE_AXIS));

        standardPanel.add(optimisePanel);
        standardPanel.add(checkPanel);
    }

    private void createButtonPanel(final String string) {
        ArrayList<VisualTransformableNode> scenarios = CpogParsingTool.getScenarios(getModel());
        m = scenarios.size();

        int value = 2;
        while (value < m) {
            value *= 2;
            bits++;
        }

        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = GUI.createDialogButton("Run");
        saveButton.addActionListener(event -> {
            setVisible(false);

            // ENCODER EXECUTION
            EncoderSettings settings = getSettings();

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
            setDone();
        });

        JButton closeButton = GUI.createDialogButton("Close");
        closeButton.addActionListener(event -> setVisible(false));

        buttonsPanel.add(saveButton);
        buttonsPanel.add(closeButton);
    }

}
