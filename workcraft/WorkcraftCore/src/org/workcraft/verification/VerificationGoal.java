package org.workcraft.verification;

import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A verification goal is a property of a model that can be verified by one or several alternative commands
 * (e.g. using different back-end tools). Cached verification results are associated with goals, not commands.
 */
public class VerificationGoal {

    public enum Scope {
        MODEL,
        HIERARCHY_ROOT,
    }

    private final String id;
    private final String title;
    private final Scope scope;
    private final boolean cacheable;
    private final Predicate<WorkspaceEntry> applicability;
    private final Function<WorkspaceEntry, String> exceptionsProvider;
    private int order = Integer.MAX_VALUE;
    private boolean reported = true;
    private VerificationGoal fallback = null;
    private Predicate<WorkspaceEntry> vacuity = null;
    private String vacuityReason = null;
    private Function<WorkspaceEntry, Collection<VerificationDependency>> dependencyProvider = null;

    public VerificationGoal(String id, String title, Scope scope, boolean cacheable,
            Predicate<WorkspaceEntry> applicability, Function<WorkspaceEntry, String> exceptionsProvider) {

        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.scope = Objects.requireNonNull(scope);
        this.cacheable = cacheable;
        this.applicability = Objects.requireNonNull(applicability);
        this.exceptionsProvider = exceptionsProvider;
    }

    public VerificationGoal(String id, String title, Predicate<WorkspaceEntry> applicability) {
        this(id, title, Scope.MODEL, true, applicability, null);
    }

    /**
     * Position of the goal among other goals in reports (the lower the earlier). It is meant for initialisation of
     * the goal, and it is independent of the order in which plugins register their goals.
     */
    public VerificationGoal setOrder(int value) {
        order = value;
        return this;
    }

    public int getOrder() {
        return order;
    }

    /**
     * Goals that are not reported are verified by commands, but are not essential for the design status report.
     * It is meant for initialisation of the goal.
     */
    public VerificationGoal setReported(boolean value) {
        reported = value;
        return this;
    }

    public boolean isReported() {
        return reported;
    }

    /**
     * Goal whose result is reported in place of the result of this goal, if the latter is missing or stale. It is
     * meant for initialisation of the goal.
     */
    public VerificationGoal setFallback(VerificationGoal value) {
        fallback = value;
        return this;
    }

    public VerificationGoal getFallback() {
        return fallback;
    }

    /**
     * Condition under which the goal holds vacuously, i.e. without verification (e.g. there are no mutex places for
     * the mutex protocol). It is meant for initialisation of the goal.
     *
     * @param reason explanation of the vacuity, e.g. "no mutex places"
     */
    public VerificationGoal setVacuity(Predicate<WorkspaceEntry> condition, String reason) {
        vacuity = condition;
        vacuityReason = reason;
        return this;
    }

    public boolean isVacuousFor(WorkspaceEntry we) {
        return (vacuity != null) && vacuity.test(we);
    }

    /**
     * Message of the result of the goal that holds vacuously.
     */
    public String getVacuityMessage() {
        return title + " holds vacuously" + ((vacuityReason == null) ? "." : " (" + vacuityReason + ").");
    }

    /**
     * Provider of the other models that results of the goal depend on, e.g. the environment of a circuit. It is
     * meant for initialisation of the goal.
     */
    public VerificationGoal setDependencies(Function<WorkspaceEntry, Collection<VerificationDependency>> provider) {
        dependencyProvider = provider;
        return this;
    }

    public Collection<VerificationDependency> getDependencies(WorkspaceEntry we) {
        return dependencyProvider == null ? Collections.emptyList() : dependencyProvider.apply(we);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Scope getScope() {
        return scope;
    }

    /**
     * Results of non-cacheable goals are quick to obtain and are re-computed when needed.
     */
    public boolean isCacheable() {
        return cacheable;
    }

    public boolean isApplicableTo(WorkspaceEntry we) {
        return applicability.test(we);
    }

    public boolean hasExceptions() {
        return exceptionsProvider != null;
    }

    /**
     * Canonical text of the exceptions that currently affect this goal for the given model.
     * It is empty if the goal has no exceptions or no exceptions are defined.
     */
    public String getExceptions(WorkspaceEntry we) {
        String result = (exceptionsProvider == null) ? null : exceptionsProvider.apply(we);
        return result == null ? "" : result;
    }

    /**
     * Title of the goal with the exceptions that were in effect when a result was obtained, in the same style as
     * the property description in the original verification report.
     */
    public String getDescription(String exceptions) {
        if ((exceptions == null) || exceptions.isEmpty()) {
            return title;
        }
        List<String> items = List.of(exceptions.split("\n"));
        return title + " with exceptions (" + TextUtils.getShortenedItems(items) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof VerificationGoal other) && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }

}
