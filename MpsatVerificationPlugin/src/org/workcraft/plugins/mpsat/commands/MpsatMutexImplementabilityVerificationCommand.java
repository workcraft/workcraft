package org.workcraft.plugins.mpsat.commands;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.AbstractVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatCombinedChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatMutexImplementabilityVerificationCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Mutex place implementability";

    @Override
    public String getDisplayName() {
        return "Mutex place implementability [MPSat]";
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
        final MainWindow mainWindow = framework.getMainWindow();
        final Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        if (stg.getMutexPlaces().isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Error: No mutex places found.",
                    TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        final ArrayList<MpsatParameters> settingsList = new ArrayList<>();
        final ArrayList<StgPlace> problematicPlaces = new ArrayList<>();
        for (StgPlace place: stg.getMutexPlaces()) {
            Mutex mutex = MutexUtils.getMutex(stg, place);
            if (mutex != null) {
                MpsatParameters settings = MpsatParameters.getImplicitMutexSettings(mutex);
                settingsList.add(settings);
            } else {
                problematicPlaces.add(place);
            }
        }
        if (!problematicPlaces.isEmpty()) {
            String problematicPlacesString = ReferenceHelper.getNodesAsString(stg, (Collection) problematicPlaces, 50);
            JOptionPane.showMessageDialog(mainWindow,
                    "Error: A mutex place must precede a pair of\n" +
                            "output transitions, each with a single trigger.\n\n" +
                            "Problematic places are:" +
                            (problematicPlacesString.length() > 30 ? "\n" : " ") +
                            problematicPlacesString,
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
        Collection<Mutex> mutexes = MutexUtils.getMutexes(stg);
        final MpsatCombinedChainResultHandler monitor = new MpsatCombinedChainResultHandler(mpsatTask, mutexes);
        taskManager.queue(mpsatTask, description, monitor);
    }

}
