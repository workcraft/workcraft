package org.workcraft.presets;

import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

@SuppressWarnings("serial")
public class PresetManagerPanel<T> extends JPanel {

    class PresetComboBox extends JComboBox<Preset<T>> {
        @Override
        public Preset<T> getSelectedItem() {
            return (Preset<T>) super.getSelectedItem();
        }
    }

    class PresetRenderer implements ListCellRenderer<Preset<T>> {
        private static final String PRESERVE_PREFIX = "&#x2713; ";
        private static final String EXAMPLE_PREFIX = "&#xBB; ";
        private static final String EXTERNAL_SUFFIX = " &#x2190; ";

        private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList<? extends Preset<T>> list,
                Preset<T> value, int index, boolean isSelected, boolean cellHasFocus) {

            Component component = renderer.getListCellRendererComponent(list,
                    getDescription(value), index, isSelected, cellHasFocus);

            File file = value.getFile();
            renderer.setToolTipText(file == null ? null : file.getAbsolutePath());
            component.setEnabled(!presetManager.isExamplePreset(value));
            return component;
        }

        private String getDescription(Preset<T> preset) {
            if (preset == null) {
                return "";
            }
            String prefix = presetManager.isPreservedPreset(preset) ? PRESERVE_PREFIX
                    : presetManager.isExamplePreset(preset) ? EXAMPLE_PREFIX : "";

            File file = preset.getFile();
            String suffix = "";
            if (file != null) {
                String decoratedFileName = file.exists() ? file.getName()
                        : TextUtils.getHtmlSpanColor(file.getName(), Color.RED);

                suffix =  EXTERNAL_SUFFIX + "<i>" + decoratedFileName + "</i>";
            }
            return "<html>" + prefix + preset.getDescription() + suffix + "</html>";
        }
    }

    private final PresetManager<T> presetManager;
    private final DataMapper<T> guiMapper;
    private final PresetComboBox presetCombo = new PresetComboBox();
    private final PresetRenderer presetRenderer = new PresetRenderer();

    public PresetManagerPanel(PresetManager<T> presetManager, DataMapper<T> guiMapper) {
        super();
        this.guiMapper = guiMapper;
        this.presetManager = presetManager;
        initialise();
    }

    private void initialise() {
        JButton duplicateButton = GuiUtils.createDialogButton("Save as...");
        duplicateButton.setToolTipText("Save selected preset with a new name");
        duplicateButton.addActionListener(event -> duplicatePreset());

        JButton updateButton = GuiUtils.createDialogButton("Update");
        updateButton.addActionListener(event -> updatePreset());

        JButton renameButton = GuiUtils.createDialogButton("Rename...");
        renameButton.addActionListener(event -> renamePreset());

        JButton deleteButton = GuiUtils.createDialogButton("Delete");
        deleteButton.addActionListener(event -> deletePreset());

        JButton linkButton = GuiUtils.createDialogButton("Link...");
        linkButton.addActionListener(event -> linkPreset());

        // Use 0 gap in FlowLayout to remove the gap after the last button and explicitly add gaps between the buttons.
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonsPanel.add(duplicateButton);
        buttonsPanel.add(GuiUtils.createHGap());
        buttonsPanel.add(updateButton);
        buttonsPanel.add(GuiUtils.createHGap());
        buttonsPanel.add(renameButton);
        buttonsPanel.add(GuiUtils.createHGap());
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(GuiUtils.createHGap());
        buttonsPanel.add(linkButton);

        fillPresetComboAndSelect(null);
        // Assign listener only after the initial fill-in, so actions are not triggered while controls are still being created
        presetCombo.addActionListener(event -> setButtonsState(updateButton, renameButton, deleteButton, linkButton));

        setBorder(GuiUtils.getTitledBorder("Presets"));
        setLayout(GuiUtils.createBorderLayout());
        add(presetCombo, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void fillPresetComboAndSelect(Preset<T> selectedPreset) {
        presetCombo.setRenderer(presetRenderer);
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

    private String askPresetDescription() {
        Preset<T> preset = getSelectedPreset();
        if (preset == null) {
            return null;
        }
        String message = "Enter preset unique name:";
        String title = "Preset description";
        String description = preset.getDescription();
        return DialogUtils.showInput(message, title, description);
    }

    private void duplicatePreset() {
        String description = askPresetDescription();
        if ((description != null) && !description.isEmpty()) {
            T data = guiMapper.getDataFromControls();
            Preset<T> newPreset = new Preset<>(description, data);
            Preset<T> savedPreset = presetManager.duplicatePreset(newPreset);
            if (savedPreset != null) {
                fillPresetComboAndSelect(savedPreset);
            }
        }
    }

    private void updatePreset() {
        Preset<T> preset = getSelectedPreset();
        if (preset != null) {
            presetManager.updatePreset(preset, guiMapper.getDataFromControls());
            fillPresetComboAndSelect(preset);
        }
    }

    private void renamePreset() {
        Preset<T> preset = getSelectedPreset();
        if (preset != null) {
            String description = askPresetDescription();
            if ((description != null) && !description.isEmpty()) {
                presetManager.renamePreset(preset, description);
                fillPresetComboAndSelect(preset);
            }
        }
    }

    private void deletePreset() {
        Preset<T> preset = getSelectedPreset();
        if (preset != null) {
            String description = preset.getDescription();
            String msg = "Are you sure you want to delete preset '" + description + "'?";
            if (DialogUtils.showConfirm(msg, "Delete preset", false)) {
                presetManager.deletePreset(preset);
                fillPresetComboAndSelect(null);
            }
        }
    }

    private void linkPreset() {
        Preset<T> preset = getSelectedPreset();
        if (preset != null) {
            File file = preset.getFile();
            if (file != null) {
                file = null;
            } else {
                JFileChooser fc = DialogUtils.createFileOpener("Select file", false, null);
                if (DialogUtils.showFileOpener(fc)) {
                    file = fc.getSelectedFile();
                }
            }
            preset.setFile(file);
            presetManager.updatePreset(preset, guiMapper.getDataFromControls());
            fillPresetComboAndSelect(preset);
        }
    }

    private void setButtonsState(JButton updateButton, JButton renameButton, JButton deleteButton, JButton linkButton) {
        updateButton.setEnabled(false);
        updateButton.setToolTipText("");
        renameButton.setEnabled(false);
        renameButton.setToolTipText("");
        deleteButton.setEnabled(false);
        deleteButton.setToolTipText("");
        linkButton.setEnabled(false);
        linkButton.setToolTipText("");
        Preset<T> preset = getSelectedPreset();
        if (preset != null) {
            if (presetManager.isExamplePreset(preset)) {
                String hintText = "Cannot make changes to an example preset";
                updateButton.setToolTipText(hintText);
                renameButton.setToolTipText(hintText);
                deleteButton.setToolTipText(hintText);
                linkButton.setToolTipText(hintText);
                linkButton.setText("Link...");
            } else {
                updateButton.setEnabled(true);
                renameButton.setEnabled(true);
                renameButton.setToolTipText("Rename selected preset");
                deleteButton.setEnabled(true);
                deleteButton.setToolTipText("Delete selected preset");
                linkButton.setEnabled(true);
                if (preset.getFile() == null) {
                    linkButton.setToolTipText("Link external file that overrides preset settings");
                    linkButton.setText("Link...");
                    updateButton.setToolTipText("Update selected preset from dialog data");
                } else {
                    linkButton.setToolTipText("Unlink external file that overrides preset settings");
                    linkButton.setText("Unlink");
                    updateButton.setToolTipText("Update selected preset from settings and linked file");
                }
            }
            guiMapper.applyDataToControls(preset.getData());
        }
    }

    public void selectFirst() {
        if (presetCombo.getItemCount() > 0) {
            presetCombo.setSelectedIndex(0);
        }
    }

    public Preset<T> getSelectedPreset() {
        return presetCombo.getSelectedItem();
    }

}
