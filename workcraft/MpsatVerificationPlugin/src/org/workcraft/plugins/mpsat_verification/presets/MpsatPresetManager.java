package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.presets.PresetManager;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatPresetManager extends PresetManager<VerificationParameters> {

    private boolean allowStgPresets;

    public MpsatPresetManager(WorkspaceEntry we, String key, MpsatDataSerialiser serialiser,
            boolean allowStgPresets, VerificationParameters preservedData) {

        super(we, key, serialiser, preservedData);
        this.setAllowStgPresets(allowStgPresets);
    }

    public boolean isAllowStgPresets() {
        return allowStgPresets;
    }

    public void setAllowStgPresets(boolean allowStgPresets) {
        this.allowStgPresets = allowStgPresets;
    }

}
