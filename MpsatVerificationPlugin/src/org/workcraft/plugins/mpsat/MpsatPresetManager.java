package org.workcraft.plugins.mpsat;

import java.io.File;

import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.plugins.shared.presets.SettingsSerialiser;

public class MpsatPresetManager extends PresetManager<MpsatParameters> {

    private boolean allowStgPresets;

    public MpsatPresetManager(File presetFile, SettingsSerialiser<MpsatParameters> serialiser, boolean allowStgPresets) {
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
