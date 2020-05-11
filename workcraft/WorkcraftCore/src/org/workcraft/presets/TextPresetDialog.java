package org.workcraft.presets;

import org.workcraft.gui.controls.FlatTextArea;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class TextPresetDialog extends PresetDialog<String> {

    private PresetManagerPanel<String> presetPanel;
    private FlatTextArea textArea;

    public TextPresetDialog(Window owner, String title, PresetManager<String> presetManager) {
        super(owner, title, presetManager);
        presetPanel.selectFirst();
        textArea.setCaretPosition(0);
        textArea.requestFocus();
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
        DataMapper<String> guiMapper = new DataMapper<String>() {
            @Override
            public void applyDataToControls(String data) {
                textArea.setText(data);
                textArea.setCaretPosition(0);
                textArea.requestFocus();
                textArea.discardEditHistory();
            }

            @Override
            public String getDataFromControls() {
                return textArea.getText();
            }
        };

        return new PresetManagerPanel<>(getUserData(), guiMapper);
    }

    private JPanel createTextPanel() {
        textArea = new FlatTextArea(4);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JPanel panel = new JPanel(GuiUtils.createBorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public String getPresetData() {
        return textArea.getText();
    }

    public JTextArea getTextArea() {
        return textArea;
    }

}
