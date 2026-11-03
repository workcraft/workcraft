package org.workcraft.commands;

import org.workcraft.verification.VerificationGoal;
import org.workcraft.verification.VerificationGoalRegistry;

import java.util.List;

/**
 * Marker for commands whose results are expensive to obtain and therefore cached with the model.
 * The goals that the command verifies are taken from the {@link VerificationGoalRegistry}.
 * A combined command that verifies several goals in one go lists them all in the registry.
 */
public interface TimeConsumingCommand extends Command {

    default List<VerificationGoal> getVerificationGoals() {
        return VerificationGoalRegistry.getInstance().getGoals(getClass());
    }

}
