package org.workcraft.plugins.cpog.scenco;

import info.clearthought.layout.TableLayout;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.layouts.SimpleFlowLayout;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.shared.IntDocument;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ConstrainedSearchScencoDialog extends AbstractScencoDialog {

    private JLabel circuitSizeLabel;
    private JCheckBox verboseModeCheck, customEncodings, abcCheck;
    private JComboBox<String> optimiseBox;
    private JPanel generationPanel, buttonsPanel, customPanel, standardPanel;
    private JTextField numberOfSolutionsText, bitsText, circuitSizeText;
    private JTable encodingTable;
    JScrollPane scrollPane;
    private final int m;
    private int bits;
    private JRadioButton normal, fast;

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

        double[][] size = new double[][] {
            {TableLayout.FILL},
            {60, height, TableLayout.FILL, 39},
        };

        TableLayout layout = new TableLayout(size);
        layout.setHGap(3);
        layout.setVGap(3);

        JPanel content = new JPanel(layout);
        content.setBorder(SizeHelper.getEmptyBorder());

        content.add(standardPanel, "0, 0");
        content.add(generationPanel, "0 1");
        content.add(customPanel, "0 2");
        content.add(buttonsPanel, "0 3");

        setContentPane(content);

        getRootPane().registerKeyboardAction(event -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (mode != 1) {
            sizeWindow(570, 570, 200, 100);
        } else {
            sizeWindow(570, 530, 200, 100);
        }
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
            public void mouseClicked(MouseEvent e) {
                customEncodings.setSelected(customEncodings.isSelected() ? false : true);
                actionCustomEncoding();
            }
        });
        customEncodings.addActionListener(event -> actionCustomEncoding());

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
        bitsText.addActionListener(event -> actionBits());

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
                data[i][1] = data[i][1] + ScencoHelper.dontCareBit;
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
        scrollPane = new JScrollPane(encodingTable);

        customPanel = new JPanel(new BorderLayout());
        customPanel.setBorder(SizeHelper.getTitledBorder("Custom encoding"));

        JPanel propertyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        propertyPanel.add(customEncodings);
        propertyPanel.add(customEncLabel);
        propertyPanel.add(bitsLabel);
        propertyPanel.add(bitsText);

        customPanel.add(propertyPanel, BorderLayout.NORTH);
        customPanel.add(scrollPane, BorderLayout.CENTER);
        customPanel.add(exampleLabel, BorderLayout.SOUTH);

    }

    private void actionBits() {
        if (Integer.parseInt(bitsText.getText()) < bits + 1) {
            DialogUtils.showError("Bits selected are not enough to encode all scenarios.");

            bitsText.setText(String.valueOf(bits + 1));
        }
        for (int i = 0; i < m; i++) {
            String data = "";
            for (int j = 0; j < Integer.parseInt(bitsText.getText()); j++) {
                data = data + ScencoHelper.dontCareBit;
            }
            encodingTable.getModel().setValueAt(data, i, 1);
        }
    }

    private void actionCustomEncoding() {
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
        optimiseBox = new JComboBox<String>();
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
            public void mouseClicked(MouseEvent e) {
                abcCheck.setSelected(abcCheck.isSelected() ? false : true);
            }
        });

        // VERBOSE MODE INSTANTIATION
        JLabel verboseModeLabel = new JLabel(ScencoHelper.textVerboseMode);
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
        generationPanel.setBorder(SizeHelper.getTitledBorder("Search range"));

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
        saveButton.addActionListener(event -> actionSave(mode));

        JButton closeButton = GuiUtils.createDialogButton("Close");
        closeButton.addActionListener(e -> setVisible(false));

        buttonsPanel.add(saveButton);
        buttonsPanel.add(closeButton);
    }

    private void actionSave(final int mode) {
        setVisible(false);

        // ENCODER EXECUTION
        EncoderSettings settings = getSettings();

        // abc disabled
        settings.setAbcFlag(abcCheck.isSelected() ? true : false);

        // speed-up mode selection
        settings.setEffort(fast.isSelected() ? false : true);
        settings.setCostFunc(false);

        // optimise for option
        settings.setCpogSize(optimiseBox.getSelectedIndex() == 0 ? false : true);

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
        settings.setNumPO(m);
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
        setDone();
    }

    private void sizeWindow(int width, int height, int row1, int row2) {
        setMinimumSize(new Dimension(width, height));
        pack();
    }

    private void modifyCircuitSize(boolean b) {
        circuitSizeText.setEnabled(b);
        circuitSizeLabel.setVisible(b);
        circuitSizeText.setVisible(b);
    }

}
