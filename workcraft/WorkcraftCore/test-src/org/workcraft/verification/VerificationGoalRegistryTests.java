package org.workcraft.verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.commands.Command;

import java.util.List;

class VerificationGoalRegistryTests {

    private interface IndividualCommand extends Command {
    }

    private interface CombinedCommand extends Command {
    }

    private interface BetterCombinedCommand extends Command {
    }

    private static VerificationGoal createGoal(String id) {
        return new VerificationGoal(id, id, we -> true);
    }

    @Test
    void testNoPreferredCommandByDefault() {
        VerificationGoalRegistry registry = VerificationGoalRegistry.getInstance();
        VerificationGoal goal = createGoal("test.no-preference");
        registry.registerGoal(goal, List.of(IndividualCommand.class, CombinedCommand.class));
        Assertions.assertNull(registry.getPreferredCommand(goal));
    }

    @Test
    void testPreferredCommandComesFirst() {
        VerificationGoalRegistry registry = VerificationGoalRegistry.getInstance();
        VerificationGoal goal = createGoal("test.order");
        registry.registerGoal(goal, List.of(IndividualCommand.class, CombinedCommand.class), CombinedCommand.class);
        Assertions.assertEquals(List.of(CombinedCommand.class, IndividualCommand.class),
                registry.getCommandsInPreferenceOrder(goal));

        // Without a preference the commands are in the order of registration
        VerificationGoal otherGoal = createGoal("test.order-without-preference");
        registry.registerGoal(otherGoal, List.of(IndividualCommand.class, CombinedCommand.class));
        Assertions.assertEquals(List.of(IndividualCommand.class, CombinedCommand.class),
                registry.getCommandsInPreferenceOrder(otherGoal));
    }

    @Test
    void testPreferredCommandMustPerformGoal() {
        VerificationGoalRegistry registry = VerificationGoalRegistry.getInstance();
        VerificationGoal goal = createGoal("test.foreign-preference");
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                registry.registerGoal(goal, List.of(IndividualCommand.class), CombinedCommand.class));

        Assertions.assertNull(registry.getPreferredCommand(goal));
    }

    @Test
    void testPreferenceOfHigherPriorityWins() {
        VerificationGoalRegistry registry = VerificationGoalRegistry.getInstance();
        VerificationGoal goal = createGoal("test.priority");
        registry.registerGoal(goal, List.of(IndividualCommand.class, CombinedCommand.class), CombinedCommand.class);
        Assertions.assertEquals(CombinedCommand.class, registry.getPreferredCommand(goal));

        registry.registerGoal(goal, List.of(BetterCombinedCommand.class), BetterCombinedCommand.class, 10);
        Assertions.assertEquals(BetterCombinedCommand.class, registry.getPreferredCommand(goal));

        // Preference of lower or equal priority does not replace the existing one
        registry.registerGoal(goal, List.of(IndividualCommand.class), IndividualCommand.class);
        registry.registerGoal(goal, List.of(CombinedCommand.class), CombinedCommand.class, 10);
        Assertions.assertEquals(BetterCombinedCommand.class, registry.getPreferredCommand(goal));
        Assertions.assertEquals(3, registry.getCommands(goal).size());
    }

}
