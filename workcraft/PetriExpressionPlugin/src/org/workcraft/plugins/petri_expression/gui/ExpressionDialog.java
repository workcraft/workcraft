package org.workcraft.plugins.petri_expression.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.petri_expression.presets.ExpressionParameters;
import org.workcraft.plugins.petri_expression.utils.ExpressionUtils;
import org.workcraft.presets.DataMapper;
import org.workcraft.presets.PresetDialog;
import org.workcraft.presets.PresetManager;
import org.workcraft.presets.PresetManagerPanel;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ExpressionDialog extends PresetDialog<ExpressionParameters> {

    private PresetManagerPanel<ExpressionParameters> presetPanel;
    private JComboBox<ExpressionParameters.Mode> modeCombo;
    private CodePanel codePanel;

    public ExpressionDialog(Window owner, PresetManager presetManager) {
        super(owner, "Petri expression", presetManager);
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

        // TODO: Populate expression presets
        addExample(presetManager, "Concurrency between transitions",
                "// Transitions a and b are concurrent\n" + "a | b");

        addExample(presetManager, "Mutual exclusion of transitions",
                "// Transitions a and b are mutually exclusive\n" + "a # b");

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
                ExpressionParameters.Mode.FAST, expression);

        presetManager.addExamplePreset(title, parameters);
    }

    private JPanel createOptionsPanel() {
        modeCombo = new JComboBox<>();
        modeCombo.setEditable(false);
        modeCombo.addItem(ExpressionParameters.Mode.FAST);
        modeCombo.addItem(ExpressionParameters.Mode.EXACT);

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
