package org.workcraft.plugins.mpsat;

import org.workcraft.presets.DataSerialiser;
import org.workcraft.presets.PresetManager;

import java.io.File;

public class MpsatPresetManager extends PresetManager<VerificationParameters> {

    private boolean allowStgPresets;

    public MpsatPresetManager(File presetFile, DataSerialiser<VerificationParameters> serialiser,
            boolean allowStgPresets, VerificationParameters preservedData) {

        super(presetFile, serialiser, preservedData);
        this.setAllowStgPresets(allowStgPresets);
    }

    public boolean isAllowStgPresets() {
        return allowStgPresets;
    }

    public void setAllowStgPresets(boolean allowStgPresets) {
        this.allowStgPresets = allowStgPresets;
    }

}
