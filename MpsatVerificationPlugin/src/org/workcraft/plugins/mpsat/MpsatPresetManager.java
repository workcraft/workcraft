package org.workcraft.plugins.mpsat;

import org.workcraft.presets.DataSerialiser;
import org.workcraft.presets.PresetManager;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatPresetManager extends PresetManager<VerificationParameters> {

    private boolean allowStgPresets;

    public MpsatPresetManager(WorkspaceEntry we, String key, DataSerialiser<VerificationParameters> serialiser,
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
