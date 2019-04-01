package org.workcraft.plugins.mpsat;

import java.io.File;

import org.workcraft.presets.PresetManager;
import org.workcraft.presets.SettingsSerialiser;

public class MpsatPresetManager extends PresetManager<VerificationParameters> {

    private boolean allowStgPresets;

    public MpsatPresetManager(File presetFile, SettingsSerialiser<VerificationParameters> serialiser, boolean allowStgPresets) {
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
