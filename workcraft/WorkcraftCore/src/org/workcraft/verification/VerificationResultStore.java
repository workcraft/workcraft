package org.workcraft.verification;

import org.workcraft.presets.DataPreserver;
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
     * Create a record for the snapshot state, without storing it.
     */
    public VerificationRecord createRecord(Snapshot snapshot, VerificationGoal goal,
            VerificationRecord.Outcome outcome, String tool, String message,
            Map<String, VerificationRecord.Dependency> dependencies) {

        String exceptions = snapshot.exceptions.get(goal.getId());
        if (exceptions == null) {
            throw new IllegalArgumentException("Snapshot does not include goal '" + goal.getId() + "'");
        }
        String modelHash = snapshot.modelHash;
        String exceptionsHash = ModelHasher.getHash(exceptions);
        return new VerificationRecord(goal.getId(), outcome, tool, message, exceptions,
                modelHash, exceptionsHash, ModelHasher.getHash(modelHash, exceptionsHash),
                dependencies, TextUtils.getCurrentTimestamp());
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
        return lookup(goal, () -> snapshot.modelHash, () -> snapshot.exceptions.get(goal.getId()), dependencyHasher);
    }

    private Lookup lookup(VerificationGoal goal, Supplier<String> modelHashSupplier,
            Supplier<String> exceptionsSupplier, Function<String, String> dependencyHasher) {

        VerificationRecord record = getRecord(goal);
        if (record == null) {
            return new Lookup(Status.MISSING, null, "no verification result is stored");
        }
        String exceptionsHash = ModelHasher.getHash(exceptionsSupplier.get());
        if (!exceptionsHash.equals(record.exceptionsHash())) {
            return stale(record, "verification exceptions were changed after the result was obtained");
        }
        String modelHash = modelHashSupplier.get();
        if (!ModelHasher.getHash(modelHash, exceptionsHash).equals(record.validityHash())) {
            return stale(record, "model was changed after the result was obtained");
        }
        for (Map.Entry<String, VerificationRecord.Dependency> dependency : record.dependencies().entrySet()) {
            String currentHash = (dependencyHasher == null) ? null : dependencyHasher.apply(dependency.getKey());
            String description = getDependencyDescription(dependency.getKey(), dependency.getValue());
            if (currentHash == null) {
                return stale(record, description + " is not available");
            }
            if (!currentHash.equals(dependency.getValue().hash())) {
                return stale(record, description + " was changed after the result was obtained");
            }
        }
        return new Lookup(Status.VALID, record, null);
    }

    /**
     * Find a result of the goal and check that it is still valid for the current state of the model and of the
     * other models that the goal depends on.
     */
    public Lookup lookup(VerificationGoal goal) {
        Map<String, VerificationDependency> dependencies = new TreeMap<>();
        for (VerificationDependency dependency : goal.getDependencies(getWorkspaceEntry())) {
            dependencies.put(dependency.name(), dependency);
        }
        Lookup result = lookup(goal, name -> {
            VerificationDependency dependency = dependencies.get(name);
            return dependency == null ? null : dependency.getHash();
        });
        // The result must be obtained with the same set of dependencies as the one that the goal has now
        if (result.isValid() && !result.getRecord().dependencies().keySet().equals(dependencies.keySet())) {
            return stale(result.getRecord(), "dependencies of the verification were changed");
        }
        return result;
    }

    private String getDependencyDescription(String name, VerificationRecord.Dependency dependency) {
        String file = dependency.file();
        String fileName = ((file == null) || file.isEmpty()) ? null : new File(file).getName();
        return "dependency '" + name + "'" + (fileName == null ? "" : " (" + fileName + ")");
    }

    private Lookup stale(VerificationRecord record, String reason) {
        return new Lookup(Status.STALE, record, reason);
    }

}
