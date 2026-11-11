package org.workcraft.verification;

import org.workcraft.Info;
import org.workcraft.presets.DataPreserver;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.ModelHasher;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Storage of cached verification results, attached to the model and saved in its .work file.
 * Results are associated with verification goals and become stale when the model math layer, the goal exceptions
 * or any of the models that the result depends on are changed.
 */
public class VerificationResultStore extends DataPreserver<Map<String, VerificationRecord>> {

    public static final String KEY = "verification-results.xml";

    /**
     * Tool of the result of a goal that holds vacuously: no backend tool is used to obtain such a result.
     */
    public static final String VACUOUS_TOOL = "";

    public enum Status {
        VALID,
        STALE,
        MISSING,
    }

    public static final class Lookup {
        private final Status status;
        private final VerificationRecord record;
        private final String reason;

        private Lookup(Status status, VerificationRecord record, String reason) {
            this.status = status;
            this.record = record;
            this.reason = reason;
        }

        public Status getStatus() {
            return status;
        }

        public boolean isValid() {
            return status == Status.VALID;
        }

        /**
         * The stored record, available for valid and stale results.
         */
        public VerificationRecord getRecord() {
            return record;
        }

        /**
         * Explanation of why a result is stale or missing.
         */
        public String getReason() {
            return reason;
        }
    }

    public VerificationResultStore(WorkspaceEntry we) {
        super(we, KEY, new VerificationRecordSerialiser());
    }

    @Override
    public Map<String, VerificationRecord> loadData() {
        Map<String, VerificationRecord> result = super.loadData();
        return result == null ? Collections.emptyMap() : result;
    }

    public VerificationRecord getRecord(VerificationGoal goal) {
        return loadData().get(goal.getId());
    }

    /**
     * State of the model and of the goal exceptions at a moment of time. Verification may take long, so a snapshot
     * is taken before it starts, and the result is associated with this state, even if the model is edited meanwhile.
     */
    public static final class Snapshot {
        private final String modelHash;
        private final Map<String, String> exceptions = new TreeMap<>();

        private Snapshot(String modelHash) {
            this.modelHash = modelHash;
        }
    }

    /**
     * Hashes of the models that a result depends on, by the keys of the dependencies, at the time of the call.
     *
     * @return null if a hash of some dependency cannot be obtained, and therefore the result cannot be validated later
     */
    public static Map<String, VerificationRecord.Dependency> getDependencySnapshot(
            Collection<VerificationDependency> dependencies) {

        Map<String, VerificationRecord.Dependency> result = new TreeMap<>();
        for (VerificationDependency dependency : dependencies) {
            String hash = dependency.getHash();
            if (hash == null) {
                return null;
            }
            File file = dependency.file();
            result.put(dependency.getKey(),
                    new VerificationRecord.Dependency(file == null ? null : FileUtils.getFullPath(file), hash));
        }
        return result;
    }

    public Snapshot takeSnapshot(Collection<VerificationGoal> goals) {
        WorkspaceEntry we = getWorkspaceEntry();
        Snapshot snapshot = new Snapshot(ModelHasher.getMathHash(we));
        for (VerificationGoal goal : goals) {
            snapshot.exceptions.put(goal.getId(), goal.getExceptions(we));
        }
        return snapshot;
    }

    /**
     * Create a record for the current state of the model and store it, replacing any previous record of the goal.
     *
     * @param dependencies other models that the result depends on, by their names
     */
    public VerificationRecord put(VerificationGoal goal, VerificationRecord.Outcome outcome, String tool,
            String message, Map<String, VerificationRecord.Dependency> dependencies) {

        return put(takeSnapshot(Collections.singleton(goal)), goal, outcome, tool, message, dependencies);
    }

    /**
     * Create a record for the snapshot state and store it, replacing any previous record of the goal.
     */
    public VerificationRecord put(Snapshot snapshot, VerificationGoal goal, VerificationRecord.Outcome outcome,
            String tool, String message, Map<String, VerificationRecord.Dependency> dependencies) {

        VerificationRecord record = createRecord(snapshot, goal, outcome, tool, message, dependencies);
        putRecords(Collections.singleton(record));
        return record;
    }

    /**
     * Create a record of the goal that holds vacuously for the current state of the model and store it.
     */
    public void putVacuous(VerificationGoal goal, Map<String, VerificationRecord.Dependency> dependencies) {
        putRecords(Collections.singleton(createVacuousRecord(takeSnapshot(Collections.singleton(goal)), goal,
                dependencies)));
    }

    /**
     * Create a record of the goal that holds vacuously for the snapshot state, without storing it.
     */
    public VerificationRecord createVacuousRecord(Snapshot snapshot, VerificationGoal goal,
            Map<String, VerificationRecord.Dependency> dependencies) {

        return createRecord(snapshot, goal, VerificationRecord.Outcome.PASS, VACUOUS_TOOL,
                goal.getVacuityMessage(), dependencies);
    }

    /**
     * Create a record for the snapshot state, without storing it.
     */
    public VerificationRecord createRecord(Snapshot snapshot, VerificationGoal goal,
            VerificationRecord.Outcome outcome, String tool, String message,
            Map<String, VerificationRecord.Dependency> dependencies) {

        String exceptions = getSnapshotExceptions(snapshot, goal);
        return new VerificationRecord(goal.getId(), outcome, TextUtils.getCurrentTimestamp(),
                tool, Info.getVersionAndEdition(), Info.getOperatingSystemDescription(),
                message, exceptions, snapshot.modelHash, dependencies);
    }

    private static String getSnapshotExceptions(Snapshot snapshot, VerificationGoal goal) {
        String result = snapshot.exceptions.get(goal.getId());
        if (result == null) {
            throw new IllegalArgumentException("Snapshot does not include goal '" + goal.getId() + "'");
        }
        return result;
    }

    /**
     * Store the records, replacing any previous records of the same goals, with a single update of the storage.
     */
    public void putRecords(Collection<VerificationRecord> newRecords) {
        Map<String, VerificationRecord> records = new TreeMap<>(loadData());
        for (VerificationRecord record : newRecords) {
            records.put(record.goalId(), record);
        }
        saveData(records);
    }

    public void remove(VerificationGoal goal) {
        Map<String, VerificationRecord> records = new TreeMap<>(loadData());
        if (records.remove(goal.getId()) != null) {
            if (records.isEmpty()) {
                clearData();
            } else {
                saveData(records);
            }
        }
    }

    /**
     * Find a result of the goal and check that it is still valid for the current state of the model.
     *
     * @param dependencyHasher gives the current hash for a dependency name, or null if it cannot be
     *                         resolved; it may be null if results of the goal never have dependencies
     */
    public Lookup lookup(VerificationGoal goal, Function<String, String> dependencyHasher) {
        WorkspaceEntry we = getWorkspaceEntry();
        return lookup(goal, () -> ModelHasher.getMathHash(we), () -> goal.getExceptions(we), dependencyHasher);
    }

    /**
     * Find a result of the goal and check that it is still valid for the state of the model that was captured in
     * the snapshot. This avoids repeated hashing of the model if the results of several goals are looked up.
     */
    public Lookup lookup(VerificationGoal goal, Snapshot snapshot, Function<String, String> dependencyHasher) {
        return lookup(goal, () -> snapshot.modelHash, () -> getSnapshotExceptions(snapshot, goal), dependencyHasher);
    }

    private Lookup lookup(VerificationGoal goal, Supplier<String> modelHashSupplier,
            Supplier<String> exceptionsSupplier, Function<String, String> dependencyHasher) {

        VerificationRecord record = getRecord(goal);
        if (record == null) {
            return new Lookup(Status.MISSING, null, "No verification result is stored");
        }
        if (!record.exceptions().equals(exceptionsSupplier.get())) {
            return stale(record, "Verification exceptions were changed " + getObtainedText(record));
        }
        if (!modelHashSupplier.get().equals(record.modelHash())) {
            return stale(record, "Model was changed " + getObtainedText(record));
        }
        for (Map.Entry<String, VerificationRecord.Dependency> dependency : record.dependencies().entrySet()) {
            String currentHash = (dependencyHasher == null) ? null : dependencyHasher.apply(dependency.getKey());
            String description = getDependencyDescription(dependency.getKey(), dependency.getValue());
            if (currentHash == null) {
                return stale(record, description + " is not available for the previous " + record.outcome()
                        + " result");
            }
            if (!currentHash.equals(dependency.getValue().hash())) {
                return stale(record, description + " was changed " + getObtainedText(record));
            }
        }
        return new Lookup(Status.VALID, record, null);
    }

    /**
     * Find a result of the goal and check that it is still valid for the current state of the model and of the
     * other models that the goal depends on.
     */
    public Lookup lookup(VerificationGoal goal) {
        return lookup(goal, VerificationDependency::getHash,
                dependencyHasher -> lookup(goal, dependencyHasher));
    }

    /**
     * Find a result of the goal and check that it is still valid for the state of the model that was captured in
     * the snapshot and for the other models that the goal depends on. This avoids repeated hashing of the model,
     * and, if the hasher of dependencies caches its results, of the dependencies if several goals are looked up.
     *
     * @param dependencyHasher gives the current hash of a dependency, or null if it cannot be obtained
     */
    public Lookup lookupWithDependencies(VerificationGoal goal, Snapshot snapshot,
            Function<VerificationDependency, String> dependencyHasher) {

        return lookup(goal, dependencyHasher, nameHasher -> lookup(goal, snapshot, nameHasher));
    }

    private Lookup lookup(VerificationGoal goal, Function<VerificationDependency, String> dependencyHasher,
            Function<Function<String, String>, Lookup> lookupFunction) {

        Map<String, VerificationDependency> dependencies = new TreeMap<>();
        for (VerificationDependency dependency : goal.getDependencies(getWorkspaceEntry())) {
            dependencies.put(dependency.getKey(), dependency);
        }
        Lookup result = lookupFunction.apply(name -> {
            VerificationDependency dependency = dependencies.get(name);
            return dependency == null ? null : dependencyHasher.apply(dependency);
        });
        // The result must be obtained with the same set of dependencies as the one that the goal has now
        if (result.isValid() && !result.getRecord().dependencies().keySet().equals(dependencies.keySet())) {
            return stale(result.getRecord(), "Verification dependencies were changed "
                    + getObtainedText(result.getRecord()));
        }
        return result;
    }

    private String getDependencyDescription(String key, VerificationRecord.Dependency dependency) {
        // The key of a dependency is empty for the environment, otherwise it is a list of component references
        StringBuilder result = new StringBuilder(key.isEmpty() ? "Environment" : "Refinement");
        String file = dependency.file();
        if ((file != null) && !file.isEmpty()) {
            result.append(" file ").append(new File(file).getName());
        }
        if (!key.isEmpty()) {
            result.append(key.contains(",") ? " for components " : " for component ").append(key);
        }
        return result.toString();
    }

    private String getObtainedText(VerificationRecord record) {
        return "after the previous " + record.outcome() + " result was obtained";
    }

    private Lookup stale(VerificationRecord record, String reason) {
        return new Lookup(Status.STALE, record, reason);
    }

}
