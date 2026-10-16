package org.workcraft.plugins.mpsat_verification;

import org.workcraft.plugins.mpsat_verification.presets.DiInterfaceDataPreserver;
import org.workcraft.plugins.mpsat_verification.presets.DiInterfaceParameters;
import org.workcraft.plugins.mpsat_verification.presets.InputPropernessDataPreserver;
import org.workcraft.plugins.mpsat_verification.presets.LocalSelfTriggeringDataPreserver;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.verification.VerificationGoal;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Verification goals for STG models. Different commands (e.g. MPSat and Determinator) may perform the same goal.
 */
public final class StgVerificationGoals {

    private static final Predicate<WorkspaceEntry> STG_APPLICABILITY
            = we -> WorkspaceUtils.isApplicable(we, StgModel.class);

    public static final VerificationGoal CONSISTENCY = new VerificationGoal(
            "stg.consistency", "Consistency", STG_APPLICABILITY).setOrder(10);

    public static final VerificationGoal OUTPUT_DETERMINACY = new VerificationGoal(
            "stg.output-determinacy", "Output determinacy", STG_APPLICABILITY).setOrder(20);

    public static final VerificationGoal INPUT_PROPERNESS = new VerificationGoal(
            "stg.input-properness", "Input properness", VerificationGoal.Scope.MODEL, true, STG_APPLICABILITY,
            we -> getCanonicalText(new InputPropernessDataPreserver(we).loadData())).setOrder(30);

    public static final VerificationGoal MUTEX_PROTOCOL = new VerificationGoal(
            "stg.mutex-protocol", "Mutex protocol", STG_APPLICABILITY).setOrder(40);

    public static final VerificationGoal OUTPUT_PERSISTENCY = new VerificationGoal(
            "stg.output-persistency", "Output persistency", STG_APPLICABILITY).setOrder(50);

    public static final VerificationGoal LOCAL_SELF_TRIGGERING = new VerificationGoal(
            "stg.local-self-triggering", "Absence of local self-triggering", VerificationGoal.Scope.MODEL, true,
            STG_APPLICABILITY, we -> getCanonicalText(new LocalSelfTriggeringDataPreserver(we).loadData()))
            .setOrder(60);

    public static final VerificationGoal DEADLOCK_FREENESS = new VerificationGoal(
            "stg.deadlock-freeness", "Deadlock freeness", STG_APPLICABILITY).setOrder(70);

    public static final VerificationGoal DETERMINISED_DEADLOCK_FREENESS = new VerificationGoal(
            "stg.determinised-deadlock-freeness", "Deadlock freeness after determinisation", STG_APPLICABILITY)
            .setOrder(80);

    public static final VerificationGoal DI_INTERFACE = new VerificationGoal(
            "stg.di-interface", "Delay insensitive interface", VerificationGoal.Scope.MODEL, true,
            STG_APPLICABILITY, StgVerificationGoals::getDiInterfaceExceptions).setOrder(90);

    private StgVerificationGoals() {
    }

    /**
     * Exceptions are taken as stored, without discarding outdated items, to avoid repeated warnings. They change
     * only when the user edits them, and any change of the model invalidates cached results anyway.
     */
    private static String getCanonicalText(Collection<String> items) {
        return items == null ? "" : String.join("\n", SortUtils.getSortedNatural(items));
    }

    private static String getDiInterfaceExceptions(WorkspaceEntry we) {
        DiInterfaceParameters data = new DiInterfaceDataPreserver(we).loadData();
        if (data == null) {
            return "";
        }
        return data.getOrderedExceptionSignalSets().stream()
                .map(signals -> "{" + String.join(", ", signals) + "}")
                .reduce((a, b) -> a + "\n" + b).orElse("");
    }

    /**
     * Goals that are verified by the combined command, in the order of verification.
     */
    public static List<VerificationGoal> getCombinedGoals(boolean noDummies) {
        if (noDummies) {
            return List.of(CONSISTENCY, DEADLOCK_FREENESS, INPUT_PROPERNESS, MUTEX_PROTOCOL, OUTPUT_PERSISTENCY,
                    LOCAL_SELF_TRIGGERING, DI_INTERFACE, OUTPUT_DETERMINACY);
        }
        return List.of(CONSISTENCY, DEADLOCK_FREENESS, MUTEX_PROTOCOL, OUTPUT_DETERMINACY);
    }

}
