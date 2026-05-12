package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.presets.DataPreserver;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DiInterfaceDataPreserver extends DataPreserver<DiInterfaceParameters> {

    public DiInterfaceDataPreserver(WorkspaceEntry we) {
        super(we, "delay-insensitive-interface.xml", new DiInterfaceDataSerialiser());
    }

    @Override
    public DiInterfaceParameters loadValidDataOnly() {
        DiInterfaceParameters data = super.loadValidDataOnly();
        Stg stg = WorkspaceUtils.getAs(getWorkspaceEntry(), Stg.class);
        Set<String> inputSignals = stg.getSignalReferences(Signal.Type.INPUT);
        List<Set<String>> validSignalSets = new ArrayList<>();
        List<String> invalidItems = new ArrayList<>();
        for (TreeSet<String> itemTreeSet : data.getOrderedExceptionSignalSets()) {
            if (itemTreeSet != null) {
                if (inputSignals.containsAll(itemTreeSet)) {
                    validSignalSets.add(itemTreeSet);
                } else {
                    invalidItems.add("{" + String.join(", ", itemTreeSet) + "}");
                }
            }
        }
        if (!invalidItems.isEmpty()) {
            String message = "Input properness is checked without outdated exception";
            LogUtils.logWarning(TextUtils.wrapMessageWithItems(message, invalidItems));
        }
        return new DiInterfaceParameters(validSignalSets);
    }

}
