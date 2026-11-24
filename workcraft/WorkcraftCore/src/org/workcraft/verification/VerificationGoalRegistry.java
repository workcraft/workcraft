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
    private final Map<String, PreferredCommand> preferredCommands = new LinkedHashMap<>();
    private final Map<String, Map<Class<? extends Command>, Integer>> commandPriorities = new LinkedHashMap<>();
    private final Map<String, Map<String, Class<? extends Command>>> toolchainPreferredCommands = new LinkedHashMap<>();
    private final Map<Class<? extends Command>, String> commandToolchains = new LinkedHashMap<>();

    private record PreferredCommand(Class<? extends Command> command, int priority) {
    }

    public static VerificationGoalRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register a goal together with commands that perform it. Registering an already known goal id
     * only adds the commands, so that plugins can add alternative commands for the same goal.
     */
    public synchronized void registerGoal(VerificationGoal goal, List<Class<? extends Command>> commands) {
        registerGoal(goal, commands, 0);
    }

    private void registerGoal(VerificationGoal goal, List<Class<? extends Command>> commands, int priority) {
        VerificationGoal knownGoal = goals.computeIfAbsent(goal.getId(), id -> goal);
        Set<Class<? extends Command>> knownCommands = goalCommands.computeIfAbsent(
                knownGoal.getId(), id -> new LinkedHashSet<>());

        Map<Class<? extends Command>, Integer> knownPriorities = commandPriorities.computeIfAbsent(
                knownGoal.getId(), id -> new LinkedHashMap<>());

        for (Class<? extends Command> command : commands) {
            if (knownCommands.add(command)) {
                commandGoals.computeIfAbsent(command, c -> new ArrayList<>()).add(knownGoal);
            }
            knownPriorities.merge(command, priority, Math::max);
        }
    }

    /**
     * Register a goal together with commands that perform it, and the command that is preferred for achieving it,
     * which is usually a combined verification command. A preference of higher priority replaces the one of lower
     * priority, so a plugin of a better tool can take over the default one. Of equal priorities the first wins.
     */
    public synchronized void registerGoal(VerificationGoal goal, List<Class<? extends Command>> commands,
            Class<? extends Command> preferredCommand, int priority) {

        if (!commands.contains(preferredCommand)) {
            throw new IllegalArgumentException("Preferred command '" + preferredCommand.getSimpleName()
                    + "' does not perform goal '" + goal.getId() + "'");
        }
        registerGoal(goal, commands, priority);
        PreferredCommand known = preferredCommands.get(goal.getId());
        if ((known == null) || (known.priority() < priority)) {
            preferredCommands.put(goal.getId(), new PreferredCommand(preferredCommand, priority));
        }
    }

    /**
     * The same as above, but the commands are also attributed to the toolchain, so a user can prefer it to the
     * toolchain of the highest priority. The preferred command is then the one that is used for the toolchain.
     */
    public synchronized void registerGoal(VerificationGoal goal, List<Class<? extends Command>> commands,
            Class<? extends Command> preferredCommand, int priority, String toolchain) {

        registerGoal(goal, commands, preferredCommand, priority);
        commands.forEach(command -> commandToolchains.put(command, toolchain));
        toolchainPreferredCommands.computeIfAbsent(goal.getId(), id -> new LinkedHashMap<>())
                .putIfAbsent(toolchain, preferredCommand);
    }

    public void registerGoal(VerificationGoal goal, List<Class<? extends Command>> commands,
            Class<? extends Command> preferredCommand) {

        registerGoal(goal, commands, preferredCommand, 0);
    }

    /**
     * Command that is preferred for achieving the goal, or null if no preference is declared.
     */
    public synchronized Class<? extends Command> getPreferredCommand(VerificationGoal goal) {
        PreferredCommand result = preferredCommands.get(goal.getId());
        return result == null ? null : result.command();
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

    /**
     * Commands that perform the goal, starting with the preferred one (if it is declared). The other commands follow
     * by the priority they were registered with, and then in the order of registration.
     */
    public synchronized List<Class<? extends Command>> getCommandsInPreferenceOrder(VerificationGoal goal) {
        Set<Class<? extends Command>> result = new LinkedHashSet<>();
        PreferredCommand preferred = preferredCommands.get(goal.getId());
        if (preferred != null) {
            result.add(preferred.command());
        }
        Map<Class<? extends Command>, Integer> priorities = commandPriorities.getOrDefault(goal.getId(), Map.of());
        List<Class<? extends Command>> others = getCommands(goal);
        others.sort(Comparator.comparingInt((Class<? extends Command> command) -> priorities.getOrDefault(command, 0))
                .reversed());

        result.addAll(others);
        return new ArrayList<>(result);
    }

    /**
     * The same as above, but the commands of the toolchain go first, starting with the one that is preferred for
     * the toolchain. The other commands follow, so the goal is achieved even if the toolchain cannot do it.
     */
    public synchronized List<Class<? extends Command>> getCommandsInPreferenceOrder(VerificationGoal goal,
            String toolchain) {

        List<Class<? extends Command>> commands = getCommandsInPreferenceOrder(goal);
        if (toolchain == null) {
            return commands;
        }
        Set<Class<? extends Command>> result = new LinkedHashSet<>();
        Class<? extends Command> preferred = toolchainPreferredCommands.getOrDefault(goal.getId(), Map.of())
                .get(toolchain);

        if (preferred != null) {
            result.add(preferred);
        }
        for (Class<? extends Command> command : commands) {
            if (toolchain.equals(commandToolchains.get(command))) {
                result.add(command);
            }
        }
        result.addAll(commands);
        return new ArrayList<>(result);
    }

    /**
     * Toolchains that the commands are attributed to, in the order of registration.
     */
    public synchronized Set<String> getToolchains() {
        return new LinkedHashSet<>(commandToolchains.values());
    }

    public synchronized List<Class<? extends Command>> getCommands(VerificationGoal goal) {
        Set<Class<? extends Command>> result = goalCommands.get(goal.getId());
        return result == null ? Collections.emptyList() : new ArrayList<>(result);
    }

    public synchronized void clear() {
        goals.clear();
        goalCommands.clear();
        commandGoals.clear();
        preferredCommands.clear();
        commandPriorities.clear();
        toolchainPreferredCommands.clear();
        commandToolchains.clear();
    }

}
