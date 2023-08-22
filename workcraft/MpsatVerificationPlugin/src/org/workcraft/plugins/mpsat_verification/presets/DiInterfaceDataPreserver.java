package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.presets.DataPreserver;
import org.workcraft.workspace.WorkspaceEntry;

public class DiInterfaceDataPreserver extends DataPreserver<DiInterfaceParameters> {

    public DiInterfaceDataPreserver(WorkspaceEntry we) {
        super(we, "delay-insensitive-interface.xml", new DiInterfaceDataSerialiser());
    }

}
