package org.workcraft.plugins.circuit.utils;

import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.verification.VerificationDependency;
import org.workcraft.verification.VerificationGoal;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Verification goals for a circuit (non-hierarchical) with respect to its environment STG. Different commands
 * (e.g. MPSat and Determinator) may perform the same goal.
 */
public final class CircuitVerificationGoals {

    private static final String ENVIRONMENT_DEPENDENCY = "environment";
    private static final String REFINEMENT_DEPENDENCY_PREFIX = "refinement:";

    private static final Predicate<WorkspaceEntry> CIRCUIT_APPLICABILITY
            = we -> WorkspaceUtils.isApplicable(we, Circuit.class);

    // The properties that are verified with respect to the environment are reported for non-hierarchical circuits
    private static final Predicate<WorkspaceEntry> FLAT_CIRCUIT_APPLICABILITY
            = we -> WorkspaceUtils.isApplicable(we, Circuit.class) && !isHierarchical(we);

    public static final VerificationGoal CONFORMATION = new VerificationGoal(
            "circuit.conformation", "Conformation to environment", FLAT_CIRCUIT_APPLICABILITY)
            .setOrder(10).setDependencies(CircuitVerificationGoals::getDependencies);

    /**
     * Output persistency ignores scan-out and auxiliary output ports. They are recognised by names, and the
     * recognition depends on the global settings, hence the ignored signals are the exceptions of this goal.
     */
    public static final VerificationGoal OUTPUT_PERSISTENCY = new VerificationGoal(
            "circuit.output-persistency", "Output persistency", VerificationGoal.Scope.MODEL, true,
            FLAT_CIRCUIT_APPLICABILITY, CircuitVerificationGoals::getOutputPersistencyExceptions)
            .setOrder(20).setDependencies(CircuitVerificationGoals::getDependencies);

    public static final VerificationGoal DEADLOCK_FREENESS = new VerificationGoal(
            "circuit.deadlock-freeness", "Deadlock freeness", FLAT_CIRCUIT_APPLICABILITY)
            .setOrder(30).setDependencies(CircuitVerificationGoals::getDependencies);

    public static final VerificationGoal DETERMINISED_DEADLOCK_FREENESS = new VerificationGoal(
            "circuit.determinised-deadlock-freeness", "Deadlock freeness after determinisation",
            FLAT_CIRCUIT_APPLICABILITY)
            .setOrder(40).setDependencies(CircuitVerificationGoals::getDependencies);

    /**
     * The circuit is a refinement of its environment STG, which is used as the specification.
     */
    public static final VerificationGoal REFINEMENT = new VerificationGoal(
            "circuit.refinement", "Refinement of environment", CIRCUIT_APPLICABILITY)
            .setOrder(60).setReported(false).setDependencies(CircuitVerificationGoals::getDependencies);

    /**
     * The gates of the circuit implement the signals of its environment STG strictly.
     */
    public static final VerificationGoal STRICT_IMPLEMENTATION = new VerificationGoal(
            "circuit.strict-implementation", "Strict implementation", CIRCUIT_APPLICABILITY)
            .setOrder(70).setReported(false).setDependencies(CircuitVerificationGoals::getDependencies);

    /**
     * N-way conformation of the reference STGs of the circuit components (and of its environment), which makes
     * sense only for a hierarchical circuit, i.e. one with components that have refinement models.
     */
    public static final VerificationGoal NWAY_CONFORMATION = new VerificationGoal(
            "circuit.nway-conformation", "N-way conformation", VerificationGoal.Scope.HIERARCHY_ROOT, true,
            CircuitVerificationGoals::isHierarchical, null)
            .setOrder(50).setDependencies(CircuitVerificationGoals::getHierarchyDependencies);

    private CircuitVerificationGoals() {
    }

    private static boolean isHierarchical(WorkspaceEntry we) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        return (circuit != null) && circuit.getFunctionComponents().stream()
                .anyMatch(component -> component.getRefinementFile() != null);
    }

    private static String getOutputPersistencyExceptions(WorkspaceEntry we) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        if (circuit == null) {
            return "";
        }
        return String.join("\n", SortUtils.getSortedNatural(ScanUtils.getScanoutAndAuxiliarySignals(circuit)));
    }

    /**
     * Results of verifying a circuit depend on its environment STG, which is an external file. The environment may
     * be absent, which is a valid state that has its own hash.
     */
    public static List<VerificationDependency> getDependencies(WorkspaceEntry we) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        return List.of(new VerificationDependency(ENVIRONMENT_DEPENDENCY, circuit.getEnvironmentFile()));
    }

    /**
     * Results of verifying a hierarchical circuit depend on the environment STG and on the refinement model of
     * each component. The composition is two-level, i.e. it does not look into the models referenced by refinements.
     */
    public static List<VerificationDependency> getHierarchyDependencies(WorkspaceEntry we) {
        List<VerificationDependency> result = new ArrayList<>(getDependencies(we));
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            File file = component.getRefinementFile();
            if (file != null) {
                String name = REFINEMENT_DEPENDENCY_PREFIX + circuit.getComponentReference(component);
                result.add(new VerificationDependency(name, file));
            }
        }
        return result;
    }

    /**
     * Goals that are verified by the combined command, in the order of verification.
     */
    public static List<VerificationGoal> getGoals(boolean conformation, boolean persistency, boolean deadlock,
            boolean determinisedDeadlock) {

        List<VerificationGoal> result = new ArrayList<>();
        if (conformation) {
            result.add(CONFORMATION);
        }
        if (persistency) {
            result.add(OUTPUT_PERSISTENCY);
        }
        if (deadlock) {
            result.add(DEADLOCK_FREENESS);
        }
        if (determinisedDeadlock) {
            result.add(DETERMINISED_DEADLOCK_FREENESS);
        }
        return result;
    }

}
