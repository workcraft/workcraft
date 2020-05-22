package org.workcraft.plugins.mpsat_verification.utils;

import org.w3c.dom.Document;
import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.MpsatDataSerialiser;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatSyntaxCheckTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.presets.PresetManager;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class MpsatUtils {

    public static  String getToolchainDescription(String title) {
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
            DialogUtils.showError("A mutex place must precede two transitions of distinct\n" +
                    "output or internal signals, each with a single trigger.\n\n" +
                    TextUtils.wrapMessageWithItems("Problematic place", problematicPlacesRefs));

            return false;
        }
        return true;
    }

    public static VerificationParameters deserialiseData(String data, MpsatDataSerialiser dataSerialiser) {
        String description = "Custom REACH assertion";
        if (data.startsWith("<") && data.endsWith(">")) {
            Document document = PresetManager.buildPresetDocumentFromSettings(description, data);
            return dataSerialiser.fromXML(document.getDocumentElement());
        }
        return new VerificationParameters(description,
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                data, true);
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
            SyntaxUtils.processBisonSyntaxError("Error: incorrect syntax of the expression: ",
                    result.getPayload(), codePanel);
        }
    }

}
