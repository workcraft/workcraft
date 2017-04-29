package org.workcraft.plugins.mpsat.commands;

import java.util.ArrayList;
import java.util.LinkedList;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatCombinedChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgMutexUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatCombinedVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Consistency, deadlock freeness, input properness and output persistency (reuse unfolding) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public final void run(WorkspaceEntry we) {
        final ArrayList<MpsatParameters> settingsList = new ArrayList<>();
        settingsList.add(MpsatParameters.getConsistencySettings());
        settingsList.add(MpsatParameters.getDeadlockSettings());
        settingsList.add(MpsatParameters.getInputPropernessSettings());

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        LinkedList<Pair<String, String>> exceptions = StgMutexUtils.getMutexGrantPairs(stg);
        settingsList.add(MpsatParameters.getOutputPersistencySettings(exceptions));

        final MpsatCombinedChainTask mpsatTask = new MpsatCombinedChainTask(we, settingsList);

        String description = "MPSat tool chain";
        String title = we.getTitle();
        if (!title.isEmpty()) {
            description += "(" + title + ")";
        }
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final MpsatCombinedChainResultHandler monitor = new MpsatCombinedChainResultHandler(mpsatTask);
        taskManager.queue(mpsatTask, description, monitor);
    }

}
