package org.workcraft.plugins.cpog.scenco;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.layouts.SimpleFlowLayout;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.shared.IntDocument;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ConstrainedSearchScencoDialog extends AbstractScencoDialog {

    private JLabel circuitSizeLabel;
    private JCheckBox verboseModeCheck;
    private JCheckBox customEncodings;
    private JCheckBox abcCheck;
    private JComboBox<String> optimiseBox;
    private JPanel generationPanel;
    private JPanel buttonsPanel;
    private JPanel customPanel;
    private JPanel standardPanel;
    private JTextField numberOfSolutionsText;
    private JTextField bitsText;
    private JTextField circuitSizeText;
    private JTable encodingTable;
    private JRadioButton normal;
    private JRadioButton fast;
    private int bits;
    private final int m;

    public ConstrainedSearchScencoDialog(Window owner, String title, EncoderSettings settings, VisualCpog model, int mode) {
        super(owner, title, settings, model);
        int height;

        ArrayList<VisualTransformableNode> scenarios = CpogParsingTool.getScenarios(model);
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

        if (mode != 1) {
            if (m < ScencoHelper.MAX_POS_FOR_SEVERAL_SYNTHESIS) {
                height = 135;
            } else {
                height = 110;
            }
        } else {
            height = 110;
        }

        JPanel content = new JPanel(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL}, new double[]{60, height, TableLayout.FILL, 39}));

        content.setBorder(GuiUtils.getEmptyBorder());

        content.add(standardPanel, new TableLayoutConstraints(0, 0));
        content.add(generationPanel, new TableLayoutConstraints(0, 1));
        content.add(customPanel, new TableLayoutConstraints(0, 2));
        content.add(buttonsPanel, new TableLayoutConstraints(0, 3));

        setContentPane(content);

        getRootPane().registerKeyboardAction(event -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (mode != 1) {
            setMinimumSize(new Dimension(570, 570));
        } else {
            setMinimumSize(new Dimension(570, 530));
        }
        pack();
        setLocationRelativeTo(owner);
    }

    private void createCustomPanel(ArrayList<VisualTransformableNode> scenarios) {
        // TABLE OF ENCODINGS
        JLabel exampleLabel = new JLabel(
                ScencoHelper.normalBitText +
                ScencoHelper.dontCareBit + ScencoHelper.dontCareBitText +
                ScencoHelper.reservedBit + ScencoHelper.reservedBitText);

        JLabel customEncLabel = new JLabel(ScencoHelper.textCustomiseLabel);
        customEncodings = new JCheckBox("", false);
        customEncLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                customEncodings.setSelected(!customEncodings.isSelected());
                customEncodingAction();
            }
        });
        customEncodings.addActionListener(event -> customEncodingAction());

        JLabel bitsLabel = new JLabel(ScencoHelper.textEncodingBitWidth);
        circuitSizeLabel = new JLabel(ScencoHelper.textCircuitSizeLabel);
        int value = 2;
        while (value < m) {
            value *= 2;
            bits++;
        }

        bitsText = new JTextField();
        bitsText.setDocument(new IntDocument(2));
        bitsText.setText(String.valueOf(bits + 1));
        bitsText.setPreferredSize(ScencoHelper.dimensionBitEncodingWidthText);
        bitsText.setBackground(Color.LIGHT_GRAY);
        bitsText.setEnabled(false);
        bitsText.addActionListener(event -> bitsAction());

        circuitSizeText = new JTextField();
        circuitSizeText.setText(String.valueOf(bits + 2));
        circuitSizeText.setPreferredSize(ScencoHelper.dimensionCircuitSizeText);
        modifyCircuitSize(false);

        String[] columnNames = {ScencoHelper.textFirstColumnTable, ScencoHelper.textSecondColumnTable };
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
                data[i][1] += ScencoHelper.dontCareBit;
            }
        }
        encodingTable = new JTable(data, columnNames);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        encodingTable.setDefaultRenderer(Object.class, renderer);
        encodingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        encodingTable.setAutoscrolls(true);
        encodingTable.setRowHeight(SizeHelper.getComponentHeightFromFont(encodingTable.getFont()));
        encodingTable.setFillsViewportHeight(true);
        encodingTable.setEnabled(false);
        encodingTable.setBackground(Color.LIGHT_GRAY);
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        encodingTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        JScrollPane scrollPane = new JScrollPane(encodingTable);

        customPanel = new JPanel(new BorderLayout());
        customPanel.setBorder(GuiUtils.getTitledBorder("Custom encoding"));

        JPanel propertyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        propertyPanel.add(customEncodings);
        propertyPanel.add(customEncLabel);
        propertyPanel.add(bitsLabel);
        propertyPanel.add(bitsText);

        customPanel.add(propertyPanel, BorderLayout.NORTH);
        customPanel.add(scrollPane, BorderLayout.CENTER);
        customPanel.add(exampleLabel, BorderLayout.SOUTH);

    }

    private void bitsAction() {
        if (Integer.parseInt(bitsText.getText()) < bits + 1) {
            DialogUtils.showError("Bits selected are not enough to encode all scenarios.");

            bitsText.setText(String.valueOf(bits + 1));
        }
        for (int i = 0; i < m; i++) {
            String value = ScencoHelper.dontCareBit.repeat(Math.max(0, Integer.parseInt(bitsText.getText())));
            encodingTable.getModel().setValueAt(value, i, 1);
        }
    }

    private void customEncodingAction() {
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

    private void createStandardPanel() {

        standardPanel = new JPanel(new SimpleFlowLayout());

        // OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
        JLabel optimiseLabel = new JLabel(ScencoHelper.textOptimiseForLabel);
        optimiseBox = new JComboBox<>();
        optimiseBox.setEditable(false);
        optimiseBox.setPreferredSize(ScencoHelper.dimensionOptimiseForBox);
        optimiseBox.addItem(ScencoHelper.textOptimiseForFirstElement);
        optimiseBox.addItem(ScencoHelper.textOptimiseForSecondElement);
        optimiseBox.setSelectedIndex(getSettings().isCpogSize() ? 0 : 1);
        optimiseBox.setBackground(Color.WHITE);

        // ABC TOOL DISABLE FLAG
        abcCheck = new JCheckBox("", getSettings().isAbcFlag());
        JLabel abcLabel = new JLabel(ScencoHelper.textAbcLabel);
        abcLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abcCheck.setSelected(!abcCheck.isSelected());
            }
        });

        // VERBOSE MODE INSTANTIATION
        JLabel verboseModeLabel = new JLabel(ScencoHelper.textVerboseMode);
        verboseModeCheck = new JCheckBox("", false);
        verboseModeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                verboseModeCheck.setSelected(!verboseModeCheck.isSelected());
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
        generationPanel.setBorder(GuiUtils.getTitledBorder("Search range"));

        // SPEED UP MODE
        fast = new JRadioButton("Synthesise only optimal (w.r.t. heuristic function) solutions (fast)");
        ButtonGroup group = new ButtonGroup();
        if (m < ScencoHelper.MAX_POS_FOR_SEVERAL_SYNTHESIS) {
            normal = new JRadioButton("Synthesise all generated solutions (slow)", true);
            group.add(normal);
        } else {
            fast.setSelected(true);
        }
        group.add(fast);

        if (mode != 1) {
            // NUMBER OF SOLUTIONS TO GENERATE
            JLabel numberOfSolutionsLabel = new JLabel(ScencoHelper.textNumberSolutionLabel);
            numberOfSolutionsText = new JTextField();
            numberOfSolutionsText.setDocument(new IntDocument(3));
            numberOfSolutionsText.setText(String.valueOf(getSettings().getSolutionNumber()));
            numberOfSolutionsText.setPreferredSize(ScencoHelper.dimensionNumberSolutionText);
            numberOfSolutionsText.setBackground(Color.WHITE);

            generationPanel.add(numberOfSolutionsLabel);
            generationPanel.add(numberOfSolutionsText);
            generationPanel.add(new SimpleFlowLayout.LineBreak());
        }
        if (m < ScencoHelper.MAX_POS_FOR_SEVERAL_SYNTHESIS) {
            generationPanel.add(normal);
            generationPanel.add(new SimpleFlowLayout.LineBreak());
        }
        generationPanel.add(fast);
    }

    private void createButtonPanel(final int mode) {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = GuiUtils.createDialogButton("Run");
        saveButton.addActionListener(event -> saveAction(mode));

        JButton closeButton = GuiUtils.createDialogButton("Close");
        closeButton.addActionListener(e -> setVisible(false));

        buttonsPanel.add(saveButton);
        buttonsPanel.add(closeButton);
    }

    private void saveAction(final int mode) {
        setVisible(false);

        // ENCODER EXECUTION
        EncoderSettings settings = getSettings();

        // abc disabled
        settings.setAbcFlag(abcCheck.isSelected());

        // speed-up mode selection
        settings.setEffort(!fast.isSelected());
        settings.setCostFunc(false);

        // optimise for option
        settings.setCpogSize(optimiseBox.getSelectedIndex() != 0);

        // verbose mode
        settings.setVerboseMode(verboseModeCheck.isSelected());

        // continuous mode or number of solutions
        if (mode != 1) {
            settings.setSolutionNumber(Integer.parseInt(numberOfSolutionsText.getText()));
        } else {
            // dummy value
            settings.setSolutionNumber(10);
        }

        // generation mode selection (Simulated annealing)
        settings.setGenerationModeInt(mode);

        // custom encodings
        if (customEncodings.isSelected()) {
            // number of bits selection
            settings.setBits(Integer.parseInt(bitsText.getText()));

            settings.setCustomEncMode(true);
            String[] encodings = new String[m];
            for (int i1 = 0; i1 < m; i1++) {
                encodings[i1] = (String) encodingTable.getModel().getValueAt(i1, 1);
            }
            for (int i2 = 0; i2 < m; i2++) {
                encodings[i2] = encodings[i2].replace(ScencoHelper.reservedBit, "-");
                encodings[i2] = encodings[i2].replace(ScencoHelper.dontCareBit, "X");
            }
            settings.setCustomEnc(encodings);
        } else {
            settings.setBits(bits + 1);
            settings.setCustomEncMode(false);
        }
    }

    private void modifyCircuitSize(boolean b) {
        circuitSizeText.setEnabled(b);
        circuitSizeLabel.setVisible(b);
        circuitSizeText.setVisible(b);
    }

}
