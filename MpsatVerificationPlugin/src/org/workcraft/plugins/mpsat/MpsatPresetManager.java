package org.workcraft.plugins.mpsat;

import java.io.File;

import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.plugins.shared.presets.SettingsSerialiser;

public class MpsatPresetManager extends PresetManager<MpsatSettings> {

    private boolean allowStgPresets;

    public MpsatPresetManager(File presetFile, SettingsSerialiser<MpsatSettings> serialiser, boolean allowStgPresets) {
        super(presetFile, serialiser);
        this.setAllowStgPresets(allowStgPresets);
    }

    public boolean isAllowStgPresets() {
        return allowStgPresets;
    }

    public void setAllowStgPresets(boolean allowStgPresets) {
        this.allowStgPresets = allowStgPresets;
    }

}
