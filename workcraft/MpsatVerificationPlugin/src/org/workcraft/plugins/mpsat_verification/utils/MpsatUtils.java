package org.workcraft.plugins.mpsat_verification.utils;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatSyntaxCheckTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class MpsatUtils {

    public static String getToolchainDescription(String title) {
        String result = "MPSat tool chain";
        if ((title != null) && !title.isEmpty()) {
            result += " (" + title + ")";
        }
        return result;
    }

    public static boolean mutexStructuralCheck(Stg stg, boolean allowEmptyMutexPlaces) {
        Collection<StgPlace> mutexPlaces = stg.getMutexPlaces();
        if (!allowEmptyMutexPlaces && mutexPlaces.isEmpty()) {
            DialogUtils.showWarning("No mutex places found to check implementability.");
            return false;
        }
        final ArrayList<StgPlace> problematicPlaces = new ArrayList<>();
        for (StgPlace place: mutexPlaces) {
            Mutex mutex = MutexUtils.getMutex(stg, place);
            if (mutex == null) {
                problematicPlaces.add(place);
            }
        }
        if (!problematicPlaces.isEmpty()) {
            Collection<String> problematicPlacesRefs = ReferenceHelper.getReferenceList(stg, problematicPlaces);
            String msg = TextUtils.wrapMessageWithItems("Failed to determine requests or grants for mutex place", problematicPlacesRefs)
                    + TextUtils.getBulletpoint("postset of mutex place must comprise rising transitions of 2 distinct output or internal signals (grants)")
                    + TextUtils.getBulletpoint("rising transitions of each grant must be triggered by rising transitions of the same signal (request)");

            DialogUtils.showError(msg, "Model validation");
            return false;
        }
        return true;
    }

    public static void checkSyntax(WorkspaceEntry we, CodePanel codePanel, VerificationParameters verificationParameters) {
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);

        MpsatSyntaxCheckTask task = new MpsatSyntaxCheckTask(verificationParameters, directory);
        TaskManager manager = Framework.getInstance().getTaskManager();
        Result<? extends ExternalProcessOutput> result = manager.execute(task, "Checking REACH assertion syntax");

        if (result.isSuccess()) {
            String message = "Property is syntactically correct";
            codePanel.showInfoStatus(message);
            LogUtils.logInfo(message);
        }

        if (result.isFailure()) {
            BisonSyntaxUtils.processSyntaxError("Error: incorrect syntax of the expression: ",
                    result.getPayload(), codePanel);
        }
    }

}
