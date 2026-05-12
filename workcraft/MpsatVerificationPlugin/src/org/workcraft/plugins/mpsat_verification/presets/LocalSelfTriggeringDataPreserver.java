package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.presets.ListDataPreserver;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalSelfTriggeringDataPreserver extends ListDataPreserver {

    public LocalSelfTriggeringDataPreserver(WorkspaceEntry we) {
        super(we, "absence-of-local-self-triggering.xml");
    }

    @Override
    public List<String> loadValidDataOnly() {
        List<String> data = super.loadValidDataOnly();
        Stg stg = WorkspaceUtils.getAs(getWorkspaceEntry(), Stg.class);
        Set<String> invalidSignals = new HashSet<>(data);
        invalidSignals.removeAll(stg.getSignalReferences(Signal.Type.OUTPUT));
        invalidSignals.removeAll(stg.getSignalReferences(Signal.Type.INTERNAL));
        if (!invalidSignals.isEmpty()) {
            data.removeAll(invalidSignals);
            String message = "Absence of local self-triggering is checked without outdated exception";
            LogUtils.logWarning(TextUtils.wrapMessageWithItems(message, SortUtils.getSortedNatural(invalidSignals)));
        }
        return data;
    }

}
