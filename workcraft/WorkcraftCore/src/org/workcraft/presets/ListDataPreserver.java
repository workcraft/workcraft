package org.workcraft.presets;

import org.workcraft.workspace.WorkspaceEntry;

import java.util.List;

public class ListDataPreserver extends DataPreserver<List<String>> {

    public ListDataPreserver(WorkspaceEntry we, String key) {
        super(we, key, new ListDataSerialiser());
    }

}
