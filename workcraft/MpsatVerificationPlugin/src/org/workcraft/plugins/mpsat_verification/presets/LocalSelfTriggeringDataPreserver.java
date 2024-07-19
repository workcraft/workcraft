package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.presets.ListDataPreserver;
import org.workcraft.workspace.WorkspaceEntry;

public class LocalSelfTriggeringDataPreserver extends ListDataPreserver {

    public LocalSelfTriggeringDataPreserver(WorkspaceEntry we) {
        super(we, "absence-of-local-self-triggering.xml");
    }

}
