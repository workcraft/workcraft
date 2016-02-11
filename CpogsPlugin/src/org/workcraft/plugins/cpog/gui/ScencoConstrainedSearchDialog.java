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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;

import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.tasks.SatBasedSolver;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.util.IntDocument;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class ScencoConstrainedSearchDialog extends JDialog {

    private JLabel numberOfSolutionsLabel, verboseModeLabel, exampleLabel,
            customEncLabel, bitsLabel, optimiseLabel,
            abcLabel, circuitSizeLabel;
    private JCheckBox verboseModeCheck, customEncodings, abcCheck;
    private JComboBox<String> optimiseBox;
    private JPanel generationPanel, buttonsPanel, content, customPanel,
            standardPanel;
    private JButton saveButton, closeButton;
    private JTextField numberOfSolutionsText, bitsText, circuitSizeText;
    private JTable encodingTable;
    JScrollPane scrollPane;
    private TableLayout layout;
    private int m, bits;
    private JRadioButton normal, fast;
    private ButtonGroup group;

    // Core variables
    private SatBasedSolver encoder;
    private EncoderSettings settings;
    private int modalResult;

    public EncoderSettings getSettings() {
        return settings;
    }

    public ScencoConstrainedSearchDialog(Window owner,
            PresetManager<EncoderSettings> presetManager,
            EncoderSettings settings, WorkspaceEntry we, String approach, int mode) {
        super(owner, approach, ModalityType.APPLICATION_MODAL);
        this.settings = settings;
        int height;
        modalResult = 0;

        VisualCPOG cpog = (VisualCPOG) (we.getModelEntry().getVisualModel());
        ArrayList<VisualTransformableNode> scenarios = CpogParsingTool.getScenarios(cpog);
        m = scenarios.size();

        /*MODE:
         * 0 - HEURISTICH APPROACH
         * 1 - EXHAUSTIVE APPROACH
         * 2 - RANDOM APPROACH
         */

        createStandardPanel();
        createGenerationPanel(mode);
        createCustomPanel(scenarios);
        createButtonPanel(mode);

        if(mode != 1){
            if(m < ScencoDialogSupport.MAX_POS_FOR_SEVERAL_SYNTHESIS){
                height = 135;
            } else {
                height = 110;
            }
        }else{
            height = 110;
        }

        double[][] size = new double[][] {{TableLayout.FILL },
                {60, height, TableLayout.FILL, 39 }, };

        layout = new TableLayout(size);
        layout.setHGap(3);
        layout.setVGap(3);

        content = new JPanel(layout);
        content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        content.add(standardPanel, "0, 0");
        content.add(generationPanel, "0 1");
        content.add(customPanel, "0 2");
        content.add(buttonsPanel, "0 3");

        setContentPane(content);

        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (mode != 1){
            sizeWindow(570, 570, 200, 100);
        }else{
            sizeWindow(570, 530, 200, 100);
        }
    }

    private void createCustomPanel(ArrayList<VisualTransformableNode> scenarios) {

        // TABLE OF ENCODINGS
        exampleLabel = new JLabel(
                ScencoDialogSupport.normalBitText +
                ScencoDialogSupport.dontCareBit + ScencoDialogSupport.dontCareBitText +
                ScencoDialogSupport.reservedBit + ScencoDialogSupport.reservedBitText);

        customEncLabel = new JLabel(ScencoDialogSupport.textCustomiseLabel);
        customEncodings = new JCheckBox("", false);
        customEncLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                customEncodings.setSelected(customEncodings.isSelected() ? false
                        : true);
                if (customEncodings.isSelected()) {
                    encodingTable.setEnabled(true);
                    encodingTable.setBackground(Color.WHITE);
                    bitsText.setBackground(Color.WHITE);
                    bitsText.setEnabled(true);
                } else {
                    encodingTable.setEnabled(false);
                    encodingTable.setBackground(Color.LIGHT_GRAY);
                    bitsText.setBackground(Color.LIGHT_GRAY);
                    bitsText.setEnabled(false);
                }
            }
        });
        customEncodings.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (customEncodings.isSelected()) {
                    encodingTable.setEnabled(true);
                    encodingTable.setBackground(Color.WHITE);
                    bitsText.setBackground(Color.WHITE);
                    bitsText.setEnabled(true);
                } else {
                    encodingTable.setEnabled(false);
                    encodingTable.setBackground(Color.LIGHT_GRAY);
                    bitsText.setBackground(Color.LIGHT_GRAY);
                    bitsText.setEnabled(false);
                }
            }
        });

        bitsLabel = new JLabel(ScencoDialogSupport.textEncodingBitWidth);
        circuitSizeLabel = new JLabel(ScencoDialogSupport.textCircuitSizeLabel);
        int value = 2;
        while (value < m) {
            value *= 2;
            bits++;
        }

        bitsText = new JTextField();
        bitsText.setDocument(new IntDocument(2));
        bitsText.setText(String.valueOf(bits + 1));
        bitsText.setPreferredSize(ScencoDialogSupport.dimensionBitEncodingWidthText);
        bitsText.setBackground(Color.LIGHT_GRAY);
        bitsText.setEnabled(false);
        bitsText.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (Integer.parseInt(bitsText.getText()) < bits + 1) {
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "Bits selected are not enough to encode all scenarios.",
                                    "Not enough bits",
                                    JOptionPane.ERROR_MESSAGE);

                    bitsText.setText(String.valueOf(bits + 1));
                }
                for (int i = 0; i < m; i++) {
                    String data = "";
                    for (int j = 0; j < Integer.parseInt(bitsText.getText()); j++)
                        data = data + ScencoDialogSupport.dontCareBit;
                    encodingTable.getModel().setValueAt(data, i, 1);
                }
            }
        });
        circuitSizeText = new JTextField();
        circuitSizeText.setText(String.valueOf(bits + 2));
        circuitSizeText.setPreferredSize(ScencoDialogSupport.dimensionCircuitSizeText);
        modifyCircuitSize(false);

        String[] columnNames = {ScencoDialogSupport.textFirstColumnTable, ScencoDialogSupport.textSecondColumnTable };
        Object[][] data = new Object[m][3];
        for (int i = 0; i < m; i++) {
            String name;
            if (scenarios.get(i).getLabel().isEmpty()) {
                name = "CPOG " + i;
            } else {
                name = scenarios.get(i).getLabel();
            }
            data[i][0] = name;
            data[i][1] = "";
            for (int j = 0; j < Integer.parseInt(bitsText.getText()); j++) {
                data[i][1] = data[i][1] + ScencoDialogSupport.dontCareBit;
            }
        }
        encodingTable = new JTable(data, columnNames);
        MyTableCellRenderer renderer = new MyTableCellRenderer();
        encodingTable.setDefaultRenderer(Object.class, renderer);
        encodingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        encodingTable.setAutoscrolls(true);
        encodingTable.setFillsViewportHeight(true);
        encodingTable.setEnabled(false);
        encodingTable.setBackground(Color.LIGHT_GRAY);
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        encodingTable.getColumnModel().getColumn(0)
                .setCellRenderer(leftRenderer);
        scrollPane = new JScrollPane(encodingTable);

        customPanel = new JPanel(new BorderLayout());
        customPanel.setBorder(BorderFactory
                .createTitledBorder("Custom encoding"));

        JPanel propertyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        propertyPanel.add(customEncodings);
        propertyPanel.add(customEncLabel);
        propertyPanel.add(bitsLabel);
        propertyPanel.add(bitsText);

        customPanel.add(propertyPanel, BorderLayout.NORTH);
        customPanel.add(scrollPane, BorderLayout.CENTER);
        customPanel.add(exampleLabel, BorderLayout.SOUTH);

    }

    private void createStandardPanel() {

        standardPanel = new JPanel(new SimpleFlowLayout());

        // OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
        optimiseLabel = new JLabel(ScencoDialogSupport.textOptimiseForLabel);
        optimiseBox = new JComboBox<String>();
        optimiseBox.setEditable(false);
        optimiseBox.setPreferredSize(ScencoDialogSupport.dimensionOptimiseForBox);
        optimiseBox.addItem(ScencoDialogSupport.textOptimiseForFirstElement);
        optimiseBox.addItem(ScencoDialogSupport.textOptimiseForSecondElement);
        optimiseBox.setSelectedIndex(settings.isCpogSize() ? 1 : 0);
        optimiseBox.setBackground(Color.WHITE);

        // ABC TOOL DISABLE FLAG
        abcCheck = new JCheckBox("", settings.isAbcFlag());
        abcLabel = new JLabel(ScencoDialogSupport.textAbcLabel);
        abcLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                abcCheck.setSelected(abcCheck.isSelected() ? false : true);
            }
        });

        // VERBOSE MODE INSTANTIATION
        verboseModeLabel = new JLabel(ScencoDialogSupport.textVerboseMode);
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
    }

    private void createGenerationPanel(final int mode) {
        generationPanel = new JPanel(new SimpleFlowLayout());
        generationPanel.setBorder(BorderFactory
                .createTitledBorder("Search range"));

        // SPEED UP MODE
        fast = new JRadioButton("Synthesise only optimal (w.r.t. heuristic function) solutions (fast)");
        group = new ButtonGroup();
        if(m < ScencoDialogSupport.MAX_POS_FOR_SEVERAL_SYNTHESIS){
            normal = new JRadioButton("Synthesise all generated solutions (slow)", true);
            group.add(normal);
        }else{
            fast.setSelected(true);
        }
        group.add(fast);

        if (mode != 1){
            // NUMBER OF SOLUTIONS TO GENERATE
            numberOfSolutionsLabel = new JLabel(ScencoDialogSupport.textNumberSolutionLabel);
            numberOfSolutionsText = new JTextField();
            numberOfSolutionsText.setDocument(new IntDocument(3));
            numberOfSolutionsText.setText(String.valueOf(settings
                    .getSolutionNumber()));
            numberOfSolutionsText.setPreferredSize(ScencoDialogSupport.dimensionNumberSolutionText);
            numberOfSolutionsText.setBackground(Color.WHITE);


            generationPanel.add(numberOfSolutionsLabel);
            generationPanel.add(numberOfSolutionsText);
            generationPanel.add(new SimpleFlowLayout.LineBreak());
        }
        if(m < ScencoDialogSupport.MAX_POS_FOR_SEVERAL_SYNTHESIS){
            generationPanel.add(normal);
            generationPanel.add(new SimpleFlowLayout.LineBreak());
        }
        generationPanel.add(fast);

    }

    private void createButtonPanel(final int mode) {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Run");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);

                // ENCODER EXECUTION

                // abc disabled
                settings.setAbcFlag(abcCheck.isSelected() ? true : false);

                // speed-up mode selection
                settings.setEffort(fast.isSelected() ? false : true);
                settings.setCostFunc(false);

                // optimise for option
                settings.setCpogSize(optimiseBox.getSelectedIndex() == 0 ? false
                        : true);

                // verbose mode
                settings.setVerboseMode(verboseModeCheck.isSelected());

                // continuous mode or number of solutions
                if (mode != 1){
                    settings.setSolutionNumber(Integer
                            .parseInt(numberOfSolutionsText.getText()));
                }else{
                    // dummy value
                    settings.setSolutionNumber(10);
                }

                // generation mode selection (Simulated annealing)
                settings.setGenerationModeInt(mode);

                // custom encodings
                settings.setNumPO(m);
                if (customEncodings.isSelected()) {
                    // number of bits selection
                    settings.setBits(Integer.parseInt(bitsText.getText()));

                    settings.setCustomEncMode(true);
                    String[] encodings = new String[m];
                    for (int i = 0; i < m; i++) {
                        encodings[i] = (String) encodingTable.getModel()
                                .getValueAt(i, 1);
                    }
                    for (int i = 0; i < m; i++) {
                        encodings[i] = encodings[i].replace(ScencoDialogSupport.reservedBit, "-");
                        encodings[i] = encodings[i].replace(ScencoDialogSupport.dontCareBit, "X");
                    }
                    settings.setCustomEnc(encodings);
                } else {
                    settings.setBits(bits + 1);
                    settings.setCustomEncMode(false);
                }

                // Set them on encoder
                encoder = new SatBasedSolver(settings);

                // execute scenco
                modalResult = 1;
            }
        });

        closeButton = new JButton("Close");
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
        pack();

    }

    private void modifyCircuitSize(boolean b) {
        circuitSizeText.setEnabled(b);
        circuitSizeLabel.setVisible(b);
        circuitSizeText.setVisible(b);
    }

    public int getModalResult() {
        return modalResult;
    }

    public SatBasedSolver getEncoder() {
        return encoder;
    }
}
