package org.workcraft.plugins.mpsat_verification.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.MpsatPresetManager;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters.SolutionMode;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.presets.DataMapper;
import org.workcraft.presets.PresetDialog;
import org.workcraft.presets.PresetManagerPanel;
import org.workcraft.shared.IntDocument;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ReachAssertionDialog extends PresetDialog<VerificationParameters> {

    private static final int DEFAULT_ALL_SOLUTION_LIMIT = 10;

    private PresetManagerPanel<VerificationParameters> presetPanel;
    private JComboBox<VerificationMode> modeCombo;
    private JTextField solutionLimitText;
    private CodePanel codePanel;
    private JRadioButton allSolutionsRadioButton;
    private JRadioButton firstSolutionRadioButton;
    private JRadioButton cheapestSolutionRadioButton;
    private JRadioButton satisfiableRadioButton;
    private JRadioButton unsatisfiableRadioButton;

    public ReachAssertionDialog(Window owner, MpsatPresetManager presetManager) {
        super(owner, "REACH assertion", presetManager);
        presetPanel.selectFirst();
        addHelpButton(new File("help/reach.html"));
        addCheckerButton(event -> MpsatUtils.checkSyntax(presetManager.getWorkspaceEntry(), codePanel, getPresetData()));
    }

    @Override
    public MpsatPresetManager getUserData() {
        return (MpsatPresetManager) super.getUserData();
    }

    @Override
    public JPanel createContentPanel() {
        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL},
                new double[]{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}));

        result.add(createOptionsPanel(), new TableLayoutConstraints(0, 1));
        result.add(createPropertyPanel(), new TableLayoutConstraints(0, 2));
        // Preset panel has to be created the last as its guiMapper refers to other controls
        presetPanel = createPresetPanel();
        result.add(presetPanel, new TableLayoutConstraints(0, 0));
        return result;
    }

    private PresetManagerPanel<VerificationParameters> createPresetPanel() {
        MpsatPresetManager presetManager = getUserData();
        if (presetManager.isAllowStgPresets()) {
            addExample(presetManager, "Mutual exclusion of arbiter grants",
                    VerificationMode.STG_REACHABILITY,
                    "// Arbiter grants are mutually exclusive\n"
                            + "// (assuming all arbiter grants are numbered\n"
                            + "// gN, where N is some number, one can use\n"
                            + "// a regular expression to find the grants)\n"
                            + "threshold[2] g in SS \"g[0-9]\\\\+\" { $g }");

            addExample(presetManager, "Mutual exclusion of signals",
                    VerificationMode.STG_REACHABILITY,
                    "// Signals u and v are mutually exclusive\n"
                            + "$S\"u\" & $S\"v\"");
        } else {
            addExample(presetManager, "Deadlock freeness",
                    VerificationMode.REACHABILITY,
                    "// All transitions are not enabled\n" +
                            "forall t in TRANSITIONS { ~@t }");

            addExample(presetManager, "Mutual exclusion of places",
                    VerificationMode.REACHABILITY,
                    "// Places p and q are mutually exclusive\n"
                            + "$P\"p\" & $P\"q\"");
        }

        DataMapper<VerificationParameters> guiMapper = new DataMapper<VerificationParameters>() {
            @Override
            public void applyDataToControls(VerificationParameters data) {
                modeCombo.setSelectedItem(data.getMode());

                switch (data.getSolutionMode()) {
                case MINIMUM_COST:
                    cheapestSolutionRadioButton.setSelected(true);
                    solutionLimitText.setText(Integer.toString(DEFAULT_ALL_SOLUTION_LIMIT));
                    solutionLimitText.setEnabled(false);
                    break;
                case FIRST:
                    firstSolutionRadioButton.setSelected(true);
                    solutionLimitText.setText(Integer.toString(DEFAULT_ALL_SOLUTION_LIMIT));
                    solutionLimitText.setEnabled(false);
                    break;
                case ALL:
                    allSolutionsRadioButton.setSelected(true);
                    int n = data.getSolutionNumberLimit();
                    solutionLimitText.setText((n > 0) ? Integer.toString(n) : "");
                    solutionLimitText.setEnabled(true);
                    break;
                }

                if (data.isInversePredicate()) {
                    unsatisfiableRadioButton.setSelected(true);
                } else {
                    satisfiableRadioButton.setSelected(true);
                }
                codePanel.setText(data.getExpression());
            }

            @Override
            public VerificationParameters getDataFromControls() {
                return getPresetData();
            }
        };

        return new PresetManagerPanel<>(presetManager, guiMapper);
    }

    private void addExample(MpsatPresetManager presetManager, String title, VerificationMode mode, String expression) {
        VerificationParameters verificationParameters = new VerificationParameters(title, mode, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                expression, true);

        presetManager.addExamplePreset(title, verificationParameters);

    }

    private JPanel createOptionsPanel() {
        JPanel result = new JPanel(GuiUtils.createBorderLayout());
        result.setBorder(GuiUtils.getTitledBorder("MPSat settings"));

        modeCombo = new JComboBox<>();
        modeCombo.setEditable(false);
        if (getUserData().isAllowStgPresets()) {
            modeCombo.addItem(VerificationMode.STG_REACHABILITY);
        }
        modeCombo.addItem(VerificationMode.REACHABILITY);

        result.add(GuiUtils.createLabeledComponent(modeCombo, "Mode:     "), BorderLayout.NORTH);

        JPanel solutionModePanel = new JPanel(GuiUtils.createNogapFlowLayout());
        solutionModePanel.add(new JLabel("Solution:"));
        cheapestSolutionRadioButton = new JRadioButton("minimise cost function");
        cheapestSolutionRadioButton.setSelected(true);
        cheapestSolutionRadioButton.addActionListener(event -> solutionLimitText.setEnabled(false));
        firstSolutionRadioButton = new JRadioButton("any");
        firstSolutionRadioButton.addActionListener(event -> solutionLimitText.setEnabled(false));
        allSolutionsRadioButton = new JRadioButton("all");
        allSolutionsRadioButton.addActionListener(event -> solutionLimitText.setEnabled(true));
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(cheapestSolutionRadioButton);
        buttonGroup.add(firstSolutionRadioButton);
        buttonGroup.add(allSolutionsRadioButton);
        solutionModePanel.add(GuiUtils.createHGap());
        solutionModePanel.add(cheapestSolutionRadioButton);
        solutionModePanel.add(GuiUtils.createHGap());
        solutionModePanel.add(firstSolutionRadioButton);
        solutionModePanel.add(GuiUtils.createHGap());
        solutionModePanel.add(allSolutionsRadioButton);

        solutionLimitText = new JTextField();
        Dimension dimension = solutionLimitText.getPreferredSize();
        dimension.width = 3 * dimension.height;
        solutionLimitText.setPreferredSize(dimension);
        solutionLimitText.setToolTipText("Maximum number of solutions. Leave blank for no limit.");
        solutionLimitText.setDocument(new IntDocument(3));
        solutionLimitText.setEnabled(false);
        solutionModePanel.add(solutionLimitText);
        result.add(solutionModePanel, BorderLayout.SOUTH);
        return result;
    }

    public JPanel createPropertyPanel() {
        JPanel resutl = new JPanel(GuiUtils.createBorderLayout());
        String title = "REACH predicate (use '" + NamespaceHelper.getHierarchySeparator() + "' as hierarchy separator)";
        resutl.setBorder(GuiUtils.getTitledBorder(title));

        codePanel = new CodePanel(6);

        satisfiableRadioButton = new JRadioButton("satisfiable");
        unsatisfiableRadioButton = new JRadioButton("unsatisfiable");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(satisfiableRadioButton);
        buttonGroup.add(unsatisfiableRadioButton);
        unsatisfiableRadioButton.setSelected(true);

        JPanel propertyPanel = new JPanel(GuiUtils.createNogapFlowLayout());
        propertyPanel.add(new JLabel("Property holds if predicate is:"));
        propertyPanel.add(GuiUtils.createHGap());
        propertyPanel.add(satisfiableRadioButton);
        propertyPanel.add(GuiUtils.createHGap());
        propertyPanel.add(unsatisfiableRadioButton);

        resutl.add(codePanel, BorderLayout.CENTER);
        resutl.add(propertyPanel, BorderLayout.SOUTH);
        return resutl;
    }

    @Override
    public VerificationParameters getPresetData() {
        SolutionMode solutionMode;
        if (firstSolutionRadioButton.isSelected()) {
            solutionMode = SolutionMode.FIRST;
        } else if (cheapestSolutionRadioButton.isSelected()) {
            solutionMode = SolutionMode.MINIMUM_COST;
        } else {
            solutionMode = SolutionMode.ALL;
        }

        int solutionLimin;
        try {
            solutionLimin = Integer.parseInt(solutionLimitText.getText());
        } catch (NumberFormatException e) {
            solutionLimin = 0;
        }
        if (solutionLimin < 0) {
            solutionLimin = 0;
        }

        return new VerificationParameters(null, (VerificationMode) modeCombo.getSelectedItem(),
                0, solutionMode, solutionLimin, codePanel.getText(), unsatisfiableRadioButton.isSelected());
    }

}
