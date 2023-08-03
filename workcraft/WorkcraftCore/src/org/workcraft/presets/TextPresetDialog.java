package org.workcraft.presets;

import org.workcraft.gui.controls.CodePanel;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class TextPresetDialog extends PresetDialog<String> {

    private PresetManagerPanel<String> presetPanel;
    private CodePanel codePanel;

    public TextPresetDialog(Window owner, String title, PresetManager<String> presetManager) {
        super(owner, title, presetManager);

        // Preset panel is set here, as it is created in overloaded createPresetPanel called from super constructor
        presetPanel.selectFirst();
    }

    @Override
    public JPanel createContentPanel() {
        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.add(createTextPanel(), BorderLayout.CENTER);
        // Preset panel has to be created the last as its guiMapper refers to other controls
        presetPanel = createPresetPanel();
        result.add(presetPanel, BorderLayout.NORTH);
        return result;
    }

    private PresetManagerPanel<String> createPresetPanel() {
        DataMapper<String> guiMapper = new DataMapper<>() {
            @Override
            public void applyDataToControls(String data) {
                codePanel.setText(data);
            }

            @Override
            public String getDataFromControls() {
                return codePanel.getText();
            }
        };

        return new PresetManagerPanel<>(getUserData(), guiMapper);
    }

    private JPanel createTextPanel() {
        codePanel = new CodePanel(11);
        JPanel panel = new JPanel(GuiUtils.createBorderLayout());
        panel.add(codePanel, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public String getPresetData() {
        return codePanel.getText();
    }

    public CodePanel getCodePanel() {
        return codePanel;
    }

}
