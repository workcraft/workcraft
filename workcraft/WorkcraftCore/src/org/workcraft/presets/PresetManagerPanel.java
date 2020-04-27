package org.workcraft.presets;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class PresetManagerPanel<T> extends JPanel {

    public static final ListCellRenderer PRESET_LIST_RENDERER = new ListCellRenderer() {

        private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            Component renderer = defaultRenderer.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);

            if (value instanceof Preset) {
                Preset preset = (Preset) value;
                if (preset.isBuiltIn()) {
                    renderer.setEnabled(false);
                }
            }
            return renderer;
        }
    };

    private final PresetManager<T> presetManager;
    private final DataMapper<T> guiMapper;
    private final JComboBox presetCombo = new JComboBox();

    public PresetManagerPanel(PresetManager<T> presetManager, DataMapper<T> guiMapper) {
        super();

        this.guiMapper = guiMapper;
        this.presetManager = presetManager;
        initialise();
    }

    private void initialise() {
        JButton createButton = GuiUtils.createDialogButton("Save as...");
        createButton.addActionListener(event -> savePreset());

        JButton updateButton = GuiUtils.createDialogButton("Update");
        updateButton.setEnabled(false);
        updateButton.addActionListener(event -> updatePreset());

        JButton renameButton = GuiUtils.createDialogButton("Rename...");
        renameButton.setEnabled(false);
        renameButton.addActionListener(event -> renamePreset());

        JButton deleteButton = GuiUtils.createDialogButton("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(event -> deletePreset());

        // Use 0 gap in FlowLayout to remove the gap after the last button and explicitly add gaps between the buttons.
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonsPanel.add(createButton);
        buttonsPanel.add(GuiUtils.createHGap());
        buttonsPanel.add(updateButton);
        buttonsPanel.add(GuiUtils.createHGap());
        buttonsPanel.add(renameButton);
        buttonsPanel.add(GuiUtils.createHGap());
        buttonsPanel.add(deleteButton);

        fillPresetComboAndSelect(null);
        // Assign listener only after the initial fill-in, so actions are not triggered while controls are still being created
        presetCombo.addActionListener(event -> setButtonsState(updateButton, renameButton, deleteButton));

        setBorder(SizeHelper.getTitledBorder("Presets"));
        setLayout(GuiUtils.createBorderLayout());
        add(presetCombo, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void fillPresetComboAndSelect(Preset selectedPreset) {
        presetCombo.setRenderer(PRESET_LIST_RENDERER);
        presetCombo.removeAllItems();
        for (Preset<T> preset : presetManager.getPresets()) {
            presetCombo.addItem(preset);
        }
        if (selectedPreset == null) {
            selectFirst();
        } else {
            presetCombo.setSelectedItem(selectedPreset);
        }
    }

    private void savePreset() {
        String description = DialogUtils.showInput("Enter preset description:", "");
        if ((description != null) && !description.isEmpty()) {
            T data = guiMapper.getDataFromControls();
            Preset<T> newPreset = new Preset<>(description, data, false);
            Preset savedPreset = presetManager.savePreset(newPreset);
            if (savedPreset != null) {
                fillPresetComboAndSelect(savedPreset);
            }
        }
    }

    private void updatePreset() {
        Preset<T> preset = (Preset<T>) presetCombo.getSelectedItem();
        presetManager.updatePreset(preset, guiMapper.getDataFromControls());
        fillPresetComboAndSelect(preset);
    }

    private void renamePreset() {
        Preset<T> preset = (Preset<T>) presetCombo.getSelectedItem();
        String description = DialogUtils.showInput("Enter preset description:", preset.getDescription());
        if ((description != null) && !description.isEmpty()) {
            presetManager.renamePreset(preset, description);
            fillPresetComboAndSelect(preset);
        }
    }

    private void deletePreset() {
        Preset<T> preset = (Preset<T>) presetCombo.getSelectedItem();
        String msg = "Are you sure you want to delete the preset \'" + preset.getDescription() + "\'?";
        if (DialogUtils.showConfirm(msg, "Delete preset", false)) {
            presetManager.removePreset(preset);
            fillPresetComboAndSelect(null);
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
