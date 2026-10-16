package org.workcraft.verification;

import org.workcraft.commands.Command;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registry of verification goals and of the commands that can perform them.
 * Plugins register goals (usually with one command, sometimes with alternatives) in their init() method.
 * A command that performs several goals in one go (a combined command) is listed for each of them.
 */
public class VerificationGoalRegistry {

    private static final VerificationGoalRegistry INSTANCE = new VerificationGoalRegistry();

    private final Map<String, VerificationGoal> goals = new LinkedHashMap<>();
    private final Map<String, Set<Class<? extends Command>>> goalCommands = new LinkedHashMap<>();
    private final Map<Class<? extends Command>, List<VerificationGoal>> commandGoals = new LinkedHashMap<>();

    public static VerificationGoalRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register a goal together with commands that perform it. Registering an already known goal id
     * only adds the commands, so that plugins can add alternative commands for the same goal.
     */
    public synchronized void registerGoal(VerificationGoal goal, List<Class<? extends Command>> commands) {
        VerificationGoal knownGoal = goals.computeIfAbsent(goal.getId(), id -> goal);
        Set<Class<? extends Command>> knownCommands = goalCommands.computeIfAbsent(
                knownGoal.getId(), id -> new LinkedHashSet<>());

        for (Class<? extends Command> command : commands) {
            if (knownCommands.add(command)) {
                commandGoals.computeIfAbsent(command, c -> new ArrayList<>()).add(knownGoal);
            }
        }
    }

    public synchronized VerificationGoal getGoal(String id) {
        return goals.get(id);
    }

    public synchronized Collection<VerificationGoal> getGoals() {
        return Collections.unmodifiableList(new ArrayList<>(goals.values()));
    }

    /**
     * Goals applicable to the model, in the order defined by the goals (and then in the order of registration).
     */
    public synchronized List<VerificationGoal> getGoals(WorkspaceEntry we) {
        List<VerificationGoal> result = new ArrayList<>();
        for (VerificationGoal goal : goals.values()) {
            if (goal.isApplicableTo(we)) {
                result.add(goal);
            }
        }
        result.sort(Comparator.comparingInt(VerificationGoal::getOrder));
        return result;
    }

    /**
     * Goals performed by the command, in the order of registration.
     */
    public synchronized List<VerificationGoal> getGoals(Class<? extends Command> command) {
        List<VerificationGoal> result = commandGoals.get(command);
        return result == null ? Collections.emptyList() : new ArrayList<>(result);
    }

    public synchronized List<Class<? extends Command>> getCommands(VerificationGoal goal) {
        Set<Class<? extends Command>> result = goalCommands.get(goal.getId());
        return result == null ? Collections.emptyList() : new ArrayList<>(result);
    }

    public synchronized void clear() {
        goals.clear();
        goalCommands.clear();
        commandGoals.clear();
    }

}
