package org.workcraft.plugins.mpsat_verification.utils;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatSyntaxCheckTask;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Triple;
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
            DialogUtils.showError("A mutex place must precede two transitions of distinct\n" +
                    "output or internal signals, each with a single trigger.\n\n" +
                    TextUtils.wrapMessageWithItems("Problematic place", problematicPlacesRefs));

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
            SyntaxUtils.processBisonSyntaxError("Error: incorrect syntax of the expression: ",
                    result.getPayload(), codePanel);
        }
    }

    public static Solution fixSolutionToggleEvents(StgModel stg, Solution solution) {
        Trace mainTrace = fixTraceToggleEvents(stg, solution.getMainTrace());
        Trace branchTrace = fixTraceToggleEvents(stg, solution.getBranchTrace());
        Solution result = new Solution(mainTrace, branchTrace, solution.getComment());
        for (Trace continuation : solution.getContinuations()) {
            result.addContinuation(fixTraceToggleEvents(stg, continuation));
        }
        return result;
    }

    public static Trace fixTraceToggleEvents(StgModel stg, Trace trace) {
        if ((trace == null) || trace.isEmpty()) {
            return trace;
        }
        Trace result = new Trace();
        for (String ref : trace) {
            String fixedRef = fixToggleEvent(stg, ref);
            if (fixedRef != null) {
                result.add(fixedRef);
            }
        }
        return result;
    }

    private static String fixToggleEvent(StgModel stg, String ref) {
        if (stg.getNodeByReference(ref) != null) {
            return ref;
        }
        Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(ref);
        if (r != null) {
            String fixedRef = r.getFirst() + r.getSecond();
            if (r.getThird() != null) {
                fixedRef += "/" + r.getThird();
            }
            if (stg.getNodeByReference(fixedRef) != null) {
                return fixedRef;
            }
        }
        return null;
    }

}
