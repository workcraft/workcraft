package org.workcraft.plugins.cflt.presets;

import org.workcraft.presets.PresetManager;
import org.workcraft.workspace.WorkspaceEntry;

public class ExpressionPresetManager extends PresetManager<ExpressionParameters> {

    private boolean allowStgPresets;

    public ExpressionPresetManager(WorkspaceEntry we, String key, ExpressionDataSerialiser serialiser,
            boolean allowStgPresets, ExpressionParameters preservedData) {

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
