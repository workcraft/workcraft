package org.workcraft.plugins.cflt.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.cflt.presets.ExpressionParameters;
import org.workcraft.presets.DataMapper;
import org.workcraft.presets.PresetDialog;
import org.workcraft.presets.PresetManager;
import org.workcraft.presets.PresetManagerPanel;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class ExpressionDialog extends PresetDialog<ExpressionParameters> {

    private PresetManagerPanel<ExpressionParameters> presetPanel;
    private JComboBox<ExpressionParameters.Mode> modeCombo;
    private CodePanel codePanel;

    public ExpressionDialog(Window owner, PresetManager presetManager,
            Consumer<CodePanel> syntaxChecker, boolean addExternalMode) {

        super(owner, "ProFlo translator", presetManager);

        // Preset panel is set here, as it is created in overloaded createPresetPanel called from super constructor
        if (addExternalMode) {
            modeCombo.insertItemAt(ExpressionParameters.Mode.EXTERNAL, 0);
        }
        presetPanel.selectFirst();
        addHelpButton(new File("help/control_flow_expressions/start.html"));
        addCheckerButton(event -> syntaxChecker.accept(codePanel));
    }

    @Override
    public JPanel createContentPanel() {
        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL},
                new double[]{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}));

        result.add(createOptionsPanel(), new TableLayoutConstraints(0, 1));
        result.add(createExpressionPanel(), new TableLayoutConstraints(0, 2));
        // Preset panel has to be created the last as its guiMapper refers to other controls
        presetPanel = createPresetPanel();
        result.add(presetPanel, new TableLayoutConstraints(0, 0));
        return result;
    }

    private PresetManagerPanel<ExpressionParameters> createPresetPanel() {
        PresetManager<ExpressionParameters> presetManager = getUserData();

        addExample(presetManager, "General information",
                "// '//' denotes a comment till the end of the line"
                + "\n// 'A | B' - concurrency of A and B"
                + "\n// 'A # B' - choice between A and B"
                + "\n// 'A ; B' or 'A B' - sequential composition of A and B"
                + "\n// '{ A }' - iteration (experimental)"
                + "\n// Operators ';', '#', and '|' are given in the order of decreasing precedences"
                + "\n// Parentheses '( ... )' can be used to override the default precedence");

        addExample(presetManager, "Concurrency between transitions",
                "// Transitions a and b are concurrent\n" + "a | b");

        addExample(presetManager, "Mutual exclusion of (choice between) transitions",
                "// Transitions a and b are mutually exclusive\n" + "a # b");

        addExample(presetManager, "Sequence of transitions",
                "// Transitions a and b are in sequence\n" + "a ; b");

        addExample(presetManager, "Sequence of transitions looped",
                "// Transitions a and b are in sequence and looped\n" + "{a ; b}");

        addExample(presetManager, "Example with all operators",
                "((i1|i2)#i3);((o1|o2)#(o3|o4)#(o5|o6|o7;o8));end");

        DataMapper<ExpressionParameters> guiMapper = new DataMapper<>() {
            @Override
            public void applyDataToControls(ExpressionParameters data) {
                modeCombo.setSelectedItem(data.getMode());
                codePanel.setText(data.getExpression());
            }

            @Override
            public ExpressionParameters getDataFromControls() {
                return getPresetData();
            }
        };

        return new PresetManagerPanel<>(presetManager, guiMapper);
    }

    private void addExample(PresetManager<ExpressionParameters> presetManager, String title, String expression) {
        ExpressionParameters parameters = new ExpressionParameters(title,
                ExpressionParameters.Mode.EXTERNAL, expression);

        presetManager.addExamplePreset(title, parameters);
    }

    private JPanel createOptionsPanel() {
        modeCombo = new JComboBox<>();
        modeCombo.setEditable(false);
        modeCombo.addItem(ExpressionParameters.Mode.FAST_MIN);
        modeCombo.addItem(ExpressionParameters.Mode.FAST_MAX);
        modeCombo.addItem(ExpressionParameters.Mode.SLOW_EXACT);
        modeCombo.addItem(ExpressionParameters.Mode.FAST_SEQ);

        JPanel result = new JPanel(GuiUtils.createBorderLayout());
        result.add(GuiUtils.createLabeledComponent(modeCombo, "Translation mode:     "), BorderLayout.NORTH);
        return result;
    }

    public JPanel createExpressionPanel() {
        codePanel = new CodePanel(11);
        JPanel result = new JPanel(GuiUtils.createBorderLayout());
        result.add(codePanel, BorderLayout.CENTER);
        return result;
    }

    @Override
    public ExpressionParameters getPresetData() {
        return new ExpressionParameters(null,
                (ExpressionParameters.Mode) modeCombo.getSelectedItem(), codePanel.getText());
    }

}
