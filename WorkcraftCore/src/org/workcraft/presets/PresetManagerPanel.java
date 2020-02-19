package org.workcraft.presets;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@SuppressWarnings("serial")
public class PresetManagerPanel<T> extends JPanel {

    private JComboBox presetCombo;

    private final PresetManager<T> presetManager;
    private final DataMapper<T> guiMapper;

    public PresetManagerPanel(PresetManager<T> presetManager, List<Preset<T>> builtinPresets, DataMapper<T> guiMapper) {
        super();

        this.guiMapper = guiMapper;
        this.presetManager = presetManager;
        if (builtinPresets != null) {
            presetManager.addFirst(builtinPresets);
        }
        initialise();
    }

    private void initialise() {
        presetCombo = new JComboBox();
        for (Preset<T> p : presetManager.getPresets()) {
            presetCombo.addItem(p);
        }

        JButton createButton = GuiUtils.createDialogButton("Save as...");
        createButton.addActionListener(event -> createPreset());

        JButton updateButton = GuiUtils.createDialogButton("Update");
        updateButton.setEnabled(false);
        updateButton.addActionListener(event -> updatePreset());

        JButton renameButton = GuiUtils.createDialogButton("Rename...");
        renameButton.setEnabled(false);
        renameButton.addActionListener(event -> renamePreset());

        JButton deleteButton = GuiUtils.createDialogButton("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(event -> deletePreset());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(createButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(renameButton);
        buttonsPanel.add(deleteButton);

        presetCombo.addActionListener(event -> setButtonsState(updateButton, renameButton, deleteButton));

        setBorder(SizeHelper.getTitledBorder("Presets"));
        setLayout(GuiUtils.createBorderLayout());
        add(presetCombo, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void createPreset() {
        String description = DialogUtils.showInput("Enter preset description:", "");
        if ((description != null) && !description.isEmpty()) {
            T data = guiMapper.getDataFromControls();
            Preset<T> preset = new Preset<>(description, data, false);
            presetManager.savePreset(preset);
            presetCombo.addItem(preset);
            presetCombo.setSelectedItem(preset);
        }
    }

    private void updatePreset() {
        Preset<T> selectedPreset = (Preset<T>) presetCombo.getSelectedItem();
        presetManager.updatePreset(selectedPreset, guiMapper.getDataFromControls());
    }

    private void renamePreset() {
        Preset<T> selectedPreset = (Preset<T>) presetCombo.getSelectedItem();
        String description = DialogUtils.showInput("Enter preset description:", selectedPreset.getDescription());
        if ((description != null) && !description.isEmpty()) {
            presetManager.renamePreset(selectedPreset, description);
        }
    }

    private void deletePreset() {
        Preset<T> selectedPreset = (Preset<T>) presetCombo.getSelectedItem();
        String msg = "Are you sure you want to delete the preset \'" + selectedPreset.getDescription() + "\'?";
        if (DialogUtils.showConfirm(msg, "Delete preset", false)) {
            presetManager.removePreset(selectedPreset);
            presetCombo.removeItem(selectedPreset);
        }
    }

    private void setButtonsState(JButton updateButton, JButton renameButton, JButton deleteButton) {
        updateButton.setEnabled(false);
        updateButton.setToolTipText("");
        renameButton.setEnabled(false);
        renameButton.setToolTipText("");
        deleteButton.setEnabled(false);
        deleteButton.setToolTipText("");
        Object selectedItem = presetCombo.getSelectedItem();
        if (selectedItem instanceof Preset) {
            Preset<T> selectedPreset = (Preset<T>) selectedItem;
            if (selectedPreset.isBuiltIn()) {
                String hintText = "Cannot make changes to a built-in preset";
                updateButton.setToolTipText(hintText);
                renameButton.setToolTipText(hintText);
                deleteButton.setToolTipText(hintText);
            } else {
                updateButton.setEnabled(true);
                updateButton.setToolTipText("Save these settings to the currently selected preset");
                renameButton.setEnabled(true);
                renameButton.setToolTipText("Rename the currently selected preset");
                deleteButton.setEnabled(true);
                deleteButton.setToolTipText("Delete the currently selected preset");
            }
            guiMapper.applyDataToControls(selectedPreset.getData());
        }
    }

    public void selectFirst() {
        if (presetCombo.getItemCount() > 0) {
            presetCombo.setSelectedIndex(0);
        }
    }

}
