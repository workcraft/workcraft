package org.workcraft.presets;

import org.workcraft.gui.dialogs.ModalDialog;

import java.awt.*;

public abstract class PresetDialog<T> extends ModalDialog<PresetManager<T>> {

    public PresetDialog(Window owner, String title, PresetManager<T> presetManager) {
        super(owner, title, presetManager);
    }

    public abstract T getPresetData();
}
