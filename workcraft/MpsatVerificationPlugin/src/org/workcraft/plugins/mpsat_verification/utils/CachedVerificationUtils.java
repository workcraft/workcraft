package org.workcraft.plugins.mpsat_verification.utils;

import org.workcraft.commands.Command;
import org.workcraft.commands.TimeConsumingCommand;
import org.workcraft.plugins.mpsat_verification.tasks.AbstractChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.ChainOutput;
import org.workcraft.tasks.Result;
import org.workcraft.verification.CachedVerification;
import org.workcraft.verification.VerificationDependency;
import org.workcraft.verification.VerificationGoal;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class CachedVerificationUtils {

    private static final String TOOL_NAME = "MPSat";

    private CachedVerificationUtils() {
    }

    /**
     * Cached verification for a command that is marked as time-consuming, or null if it is not.
     */
    public static CachedVerification create(WorkspaceEntry we, Command command) {
        if (command instanceof TimeConsumingCommand timeConsumingCommand) {
            return create(we, timeConsumingCommand.getVerificationGoals());
        }
        return null;
    }

    public static CachedVerification create(WorkspaceEntry we, List<VerificationGoal> goals) {
        return create(we, goals, Collections.emptyList());
    }

    public static CachedVerification create(WorkspaceEntry we, List<VerificationGoal> goals,
            Collection<VerificationDependency> dependencies) {

        return CachedVerification.create(we, goals, TOOL_NAME, dependencies);
    }

    /**
     * Associate the monitor with the cached verification and, if requested, finish it with a cached result.
     *
     * @return true if the monitor was finished with a cached result, and the verification must not be queued
     */
    public static <T extends ChainOutput> boolean tryReuse(AbstractChainResultHandlingMonitor<T> monitor,
            CachedVerification cachedVerification, boolean reuse, Runnable rerunAction) {

        if (cachedVerification == null) {
            return false;
        }
        monitor.setCachedVerification(cachedVerification);
        if (cachedVerification.prepare(reuse, rerunAction)) {
            monitor.isFinished(Result.<T>success());
            return true;
        }
        return false;
    }

}
