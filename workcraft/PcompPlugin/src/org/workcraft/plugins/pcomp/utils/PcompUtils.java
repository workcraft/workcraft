package org.workcraft.plugins.pcomp.utils;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.List;

public class PcompUtils {

    public static List<WorkspaceEntry> deserealiseData(String data) {
        List<WorkspaceEntry> wes = new ArrayList<>();
        Framework framework = Framework.getInstance();
        String msg = "";
        for (String word : TextUtils.splitWords(data)) {
            try {
                WorkspaceEntry we = framework.loadWork(word);
                if (we == null) {
                    msg += "\n  * " + word;
                } else {
                    wes.add(we);
                }
            } catch (DeserialisationException e) {
                msg += "\n  * " + word;
            }
        }
        if (!msg.isEmpty()) {
            wes.clear();
            LogUtils.logError("Could not load the following files:" + msg);
        }
        return wes;
    }
}
