package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.presets.DataPreserver;
import org.workcraft.workspace.WorkspaceEntry;

public class LocalSelfTriggeringDataPreserver extends DataPreserver<LocalSelfTriggeringParameters> {

    public LocalSelfTriggeringDataPreserver(WorkspaceEntry we) {
        super(we, "absence-of-local-self-triggering.xml", new LocalSelfTriggeringDataSerialiser());
    }

}
