package org.workcraft.verification;

import org.workcraft.Framework;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Reuse and caching of the results of one verification command run. The command performs one goal, or several
 * goals in one go (combined command). The state of the model is captured when this object is created, i.e. before
 * the verification starts, so that a result is never associated with a model edited during a long verification.
 * <p>
 * Reuse rules: a command with a single goal reuses its valid PASS or FAIL result. A combined command
 * is skipped only if all its goals have valid PASS results, otherwise it is re-run as a whole.
 * Likewise, a combined command stores results only if it passes, as a failure does not say which goal failed.
 * <p>
 * A result may also depend on other models (e.g. the environment of a circuit). Hashes of such dependencies are
 * stored with the result and checked on reuse. A dependency whose hash cannot be obtained makes the result
 * unusable for the cache.
 */
public class CachedVerification {

    private static final String RESULT_TITLE = "Verification results";

    private final WorkspaceEntry we;
    private final VerificationResultStore resultStore;
    private final List<VerificationGoal> goals;
    private final String tool;
    private final VerificationResultStore.Snapshot snapshot;
    private final List<VerificationRecord> reusableRecords = new ArrayList<>();
    private final Map<String, VerificationRecord.Dependency> dependencySnapshot;
    private final boolean dependenciesResolved;

    private Runnable rerunAction = null;

    public CachedVerification(WorkspaceEntry we, List<VerificationGoal> goals, String tool,
            Collection<VerificationDependency> dependencies) {

        this.we = we;
        this.resultStore = new VerificationResultStore(we);
        this.goals = new ArrayList<>(goals);
        this.tool = tool;
        this.snapshot = resultStore.takeSnapshot(this.goals);
        // Hashes of dependencies are captured before the verification, as the files may be edited during it
        Map<String, VerificationRecord.Dependency> resolvedDependencies =
                VerificationResultStore.getDependencySnapshot(dependencies);
        this.dependenciesResolved = (resolvedDependencies != null);
        this.dependencySnapshot = dependenciesResolved ? resolvedDependencies : Collections.emptyMap();
    }

    /**
     * Cache is used only if the command has goals, and all of them are cacheable.
     */
    public static CachedVerification create(WorkspaceEntry we, List<VerificationGoal> goals, String tool) {
        return create(we, goals, tool, Collections.emptyList());
    }

    /**
     * Cache is used only if the command has goals, and all of them are cacheable and applicable to the model.
     * This avoids storing results of a command that can be run on a model of a wider class than the goals are
     * defined for, e.g. results for a Petri net under the goals of an STG.
     *
     * @param dependencies the other models that the result depends on
     */
    public static CachedVerification create(WorkspaceEntry we, List<VerificationGoal> goals, String tool,
            Collection<VerificationDependency> dependencies) {

        if (goals.isEmpty() || !goals.stream().allMatch(goal -> goal.isCacheable() && goal.isApplicableTo(we))) {
            return null;
        }
        return new CachedVerification(we, goals, tool, dependencies);
    }

    public boolean isCombined() {
        return goals.size() > 1;
    }

    /**
     * Action that re-runs the verification, ignoring the cache.
     */
    public void setRerunAction(Runnable value) {
        rerunAction = value;
    }

    public Runnable getRerunAction() {
        return rerunAction;
    }

    /**
     * Look for valid results that make the verification unnecessary.
     */
    public boolean findReusableRecords() {
        reusableRecords.clear();
        if (!dependenciesResolved) {
            return false;
        }
        List<VerificationRecord> records = new ArrayList<>();
        for (VerificationGoal goal : goals) {
            VerificationResultStore.Lookup lookup = resultStore.lookup(goal, snapshot, this::getDependencyHash);
            // An undetermined result is not reused, as the user may choose to override the reason for it
            if (!lookup.isValid() || !lookup.getRecord().isDefinite()
                    || (isCombined() && !lookup.getRecord().isPass())) {
                return false;
            }
            // The result must be obtained with the same set of dependencies as the one that the command has now
            if (!lookup.getRecord().dependencies().keySet().equals(dependencySnapshot.keySet())) {
                return false;
            }
            records.add(lookup.getRecord());
        }
        reusableRecords.addAll(records);
        return true;
    }

    /**
     * The last search for reusable results has found them. The monitor of the command is then finished without
     * running the verification, and the cached outcome is reported instead of the result of a verification task.
     */
    public boolean hasReusableResult() {
        return !reusableRecords.isEmpty();
    }

    public List<VerificationRecord> getReusableRecords() {
        return Collections.unmodifiableList(reusableRecords);
    }

    /**
     * Outcome of the reusable results: true if all of them are PASS.
     */
    public boolean getReusableOutcome() {
        return reusableRecords.stream().allMatch(VerificationRecord::isPass);
    }

    /**
     * Prepare for the verification run: set the action for re-running verification, and, if the cache is to be used,
     * look for reusable results.
     *
     * @return true if the verification is not needed, and the reusable results are to be reported instead
     */
    public boolean prepare(boolean reuse, Runnable rerun) {
        if (holdsVacuously()) {
            // There is nothing to verify and, therefore, to re-run
            setRerunAction(null);
            return findReusableRecords() || storeVacuousRecords();
        }
        setRerunAction(rerun);
        return reuse && findReusableRecords();
    }

    private boolean holdsVacuously() {
        return !goals.isEmpty() && goals.stream().allMatch(goal -> goal.isVacuousFor(we));
    }

    /**
     * Store the results of the goals that hold vacuously, and make them reusable. The records are used directly
     * rather than looked up, as in GUI mode they may be stored later by the editor thread.
     */
    private boolean storeVacuousRecords() {
        if (!dependenciesResolved) {
            return false;
        }
        List<VerificationRecord> records = new ArrayList<>();
        for (VerificationGoal goal : goals) {
            records.add(resultStore.createVacuousRecord(snapshot, goal, dependencySnapshot));
        }
        putRecords(records);
        reusableRecords.clear();
        reusableRecords.addAll(records);
        return true;
    }

    /**
     * Report the reusable results to the user. In interactive mode the user may choose to re-run the verification.
     *
     * @return the outcome of the reusable results
     */
    public boolean reportReusedResult(boolean interactive) {
        boolean outcome = getReusableOutcome();
        String message = getReusableMessage();
        if (interactive && (rerunAction != null)) {
            // The note about the cached results is placed just before the question
            int messageType = outcome ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
            String question = "\n\n" + getReusableNote() + "\n\nRe-run verification afresh?";
            if (DialogUtils.showConfirm(getReusableBody(), question, RESULT_TITLE, false, messageType, true)) {
                rerunAction.run();
            }
        } else if (interactive) {
            if (outcome) {
                DialogUtils.showInfo(message, RESULT_TITLE);
            } else {
                DialogUtils.showWarning(message, RESULT_TITLE);
            }
        } else if (outcome) {
            LogUtils.logInfo(message);
        } else {
            LogUtils.logWarning(message);
        }
        return outcome;
    }

    /**
     * Description of the reusable results for the user: the message of the original report, followed by the note
     * with the time (and the tool) when the results were obtained. A result that holds vacuously is not worth such
     * a note, as it is established without verification.
     */
    public String getReusableMessage() {
        if (reusableRecords.isEmpty()) {
            return "";
        }
        boolean vacuous = reusableRecords.stream()
                .allMatch(record -> VerificationResultStore.VACUOUS_TOOL.equals(record.tool()));

        return vacuous ? getReusableBody() : getReusableBody() + "\n\n" + getReusableNote();
    }

    /**
     * Message of the original report, i.e. the message of the stored result for a single goal, or a list of
     * verified properties (with their exceptions) for a combined command.
     */
    private String getReusableBody() {
        if (reusableRecords.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        if (isCombined()) {
            result.append("The following properties have been successfully verified:");
            for (int i = 0; i < goals.size(); i++) {
                result.append(TextUtils.getBulletpoint(
                        goals.get(i).getDescription(reusableRecords.get(i).exceptions())));
            }
        } else {
            result.append(reusableRecords.getFirst().message());
        }
        return result.toString();
    }

    private String getReusableNote() {
        StringBuilder result = new StringBuilder(isCombined()
                ? "These verification results are cached."
                : "This verification result is cached.");

        List<ZonedDateTime> times = reusableRecords.stream()
                .map(VerificationRecord::getTime)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        if (!times.isEmpty()) {
            String first = VerificationRecord.formatTime(times.getFirst());
            String last = VerificationRecord.formatTime(times.getLast());
            result.append("\n- Timestamp: ").append(first.equals(last) ? first : first + " - " + last);
        }

        List<String> toolchains = reusableRecords.stream()
                .map(VerificationRecord::getToolchain)
                .filter(toolchain -> !toolchain.isEmpty())
                .distinct()
                .toList();

        if (toolchains.size() == 1) {
            result.append("\n- Toolchain: ").append(toolchains.getFirst());
        } else if (!toolchains.isEmpty()) {
            result.append("\n- Toolchains:\n   ").append(String.join(",\n   ", toolchains));
        }
        return result.toString();
    }

    /**
     * Store the result of the verification run.
     *
     * @param outcome true if the properties hold, false if some is violated, null if no definite outcome
     * @param message summary of the result of a single-goal command
     */
    public void store(Boolean outcome, String message) {
        // Results that cannot be validated later are not stored
        if ((outcome == null) || (isCombined() && !outcome) || !dependenciesResolved) {
            return;
        }
        VerificationRecord.Outcome recordOutcome = outcome ? VerificationRecord.Outcome.PASS
                : VerificationRecord.Outcome.FAIL;

        List<VerificationRecord> records = new ArrayList<>();
        for (VerificationGoal goal : goals) {
            String goalMessage = message;
            // TODO: Exceptions are stored both in the message and in the record, compose generic messages on display
            if (isCombined() || (message == null)) {
                String exceptions = VerificationResultStore.getSnapshotExceptions(snapshot, goal);
                goalMessage = goal.getDescription(exceptions) + (outcome ? " holds." : " is violated.");
            }
            records.add(resultStore.createRecord(snapshot, goal, recordOutcome, tool, goalMessage, dependencySnapshot));
        }
        putRecords(records);
    }

    /**
     * Store the undetermined result of the verification run, i.e. the verification cannot be performed on the model
     * and this is the property of the model (e.g. it is unsafe). Failures that depend on the environment (e.g. a tool
     * crash or an unreadable file) must not be stored. As a failure of a combined command, the undetermined result
     * does not say which goal is affected, hence it is stored for a single goal only.
     *
     * @param reason explanation of why the verification cannot be performed, of which only the first paragraph
     *               is stored as a single line
     */
    public void storeUndetermined(String reason) {
        if (isCombined() || !dependenciesResolved) {
            return;
        }
        putRecords(List.of(resultStore.createRecord(snapshot, goals.getFirst(),
                VerificationRecord.Outcome.UNDETERMINED, tool, getSummary(reason), dependencySnapshot)));
    }

    /**
     * The first paragraph of the text as a single line without the full stop at the end, i.e. a summary without
     * the details, such as the list of problematic items.
     */
    private static String getSummary(String text) {
        String paragraph = text.strip().split("\\R\\s*\\R", 2)[0];
        String result = paragraph.replaceAll("\\s+", " ");
        return result.endsWith(".") ? result.substring(0, result.length() - 1) : result;
    }

    private void putRecords(List<VerificationRecord> records) {
        // The results are stored with a single update. In GUI mode the storage belongs to the editor thread, as
        // the update marks the model as changed, while verification is completed by a thread of the task manager.
        if (Framework.getInstance().isInGuiMode() && !SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> resultStore.putRecords(records));
        } else {
            resultStore.putRecords(records);
        }
    }

    /**
     * Hash of a dependency at the time when this object was created, i.e. before the verification started.
     */
    private String getDependencyHash(String name) {
        VerificationRecord.Dependency dependency = dependencySnapshot.get(name);
        return dependency == null ? null : dependency.hash();
    }

}
