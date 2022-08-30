package org.workcraft.plugins.cflt.gui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.cflt.presets.ExpressionParameters;
import org.workcraft.plugins.cflt.utils.ExpressionUtils;
import org.workcraft.presets.DataMapper;
import org.workcraft.presets.PresetDialog;
import org.workcraft.presets.PresetManager;
import org.workcraft.presets.PresetManagerPanel;
import org.workcraft.utils.GuiUtils;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

public class ExpressionDialog extends PresetDialog<ExpressionParameters> {

    private PresetManagerPanel<ExpressionParameters> presetPanel;
    private JComboBox<ExpressionParameters.Mode> modeCombo;
    private CodePanel codePanel;

    public ExpressionDialog(Window owner, PresetManager presetManager) {
        super(owner, "Control Flow Logic Translator", presetManager);
        presetPanel.selectFirst();
        // TODO: Prepare Petri expression help in workcraft.org, similar to https://workcraft.org/help/reach
        addHelpButton(new File("help/petri_expression.html"));
        addCheckerButton(event -> ExpressionUtils.checkSyntax(codePanel));
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
                "// Anything starting with '//' is a comment and will be ignored"
                + "\n" + "// '|' represents concurrency"
                + "\n" + "// '#' represents mutual exclusion (choice)"
                + "\n" + "// ';' represents sequence"
                + "\n" + "// '{' and '}' enforce iteration (loops)"
                + "\n" + "// '(' and ')' enforce precedence"
                + "\n" + "\n" + "// The precedence is as follows: "
                + "\n" + "// brackets > sequence > choice > concurrency"
                + "\n" + "\n" + "// Multiple transitions with the same name are allowed");


        addExample(presetManager, "Concurrency between transitions",
                "// Transitions a and b are concurrent\n" + "a | b" + "\n//events 'a' and 'b' will take place in any order");

        addExample(presetManager, "Mutual exclusion of (choice between) transitions",
                "// Transitions a and b are mutually exclusive\n" + "a # b" + "\n//either event 'a' or 'b' will take place");

        addExample(presetManager, "Sequence of transitions",
                "// Transitions a and b are in sequence\n" + "a ; b" + "\n//event 'b' will only take place after event 'a'");
        addExample(presetManager, "Sequence of transitions looped",
                "// Transitions a and b are in sequence and looped\n" + "{a ; b}");

        addExample(presetManager, "Precedence",
                "// The precedence is as follows: brackets > sequence > choice > concurrency\n" + "a | b # c ; (d | e)");

        addExample(presetManager, "Sample",
                "// A sample expression\n" + "((i1|i2)#i3);((o1|o2)#(o3|o4)#(o5|o6|o7;o8));end");

        DataMapper<ExpressionParameters> guiMapper = new DataMapper<ExpressionParameters>() {
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
                ExpressionParameters.Mode.FAST_MIN, expression);

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
