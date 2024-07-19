package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.stg.Stg;
import org.workcraft.presets.PresetManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class HandshakePresetManager extends PresetManager<HandshakeParameters> {

    public HandshakePresetManager(WorkspaceEntry we, String key,
            HandshakeDataSerialiser serialiser, HandshakeParameters preservedData) {

        super(we, key, serialiser, preservedData);
    }

    public Stg getStg() {
        return WorkspaceUtils.getAs(getWorkspaceEntry(), Stg.class);
    }

}
