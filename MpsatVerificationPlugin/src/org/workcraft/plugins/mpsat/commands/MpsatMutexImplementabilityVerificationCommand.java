package org.workcraft.plugins.mpsat.commands;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatCombinedChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgMutexUtils;
import org.workcraft.plugins.stg.MutexData;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatMutexImplementabilityVerificationCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Implementability by mutex";

    @Override
    public String getDisplayName() {
        return "Non-persistency implementable by mutex [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        final ArrayList<MpsatParameters> settingsList = new ArrayList<>();
        for (StgPlace place: stg.getMutexPlaces()) {
            MutexData mutexData = new MutexData();
            if (StgMutexUtils.fillMutexContext(stg, place, mutexData)) {
                MpsatParameters settings = MpsatParameters.getImplicitMutexSettings(mutexData);
                settingsList.add(settings);
            }
        }
        if (settingsList.isEmpty()) {
            JOptionPane.showMessageDialog(framework.getMainWindow(),
                    "Error: No mutex place found with non-input grants and unique requests.",
                    TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        final MpsatCombinedChainTask mpsatTask = new MpsatCombinedChainTask(we, settingsList);

        String description = "MPSat tool chain";
        String title = we.getTitle();
        if (!title.isEmpty()) {
            description += "(" + title + ")";
        }
        final TaskManager taskManager = framework.getTaskManager();
        final MpsatCombinedChainResultHandler monitor = new MpsatCombinedChainResultHandler(mpsatTask);
        taskManager.queue(mpsatTask, description, monitor);
    }

}
