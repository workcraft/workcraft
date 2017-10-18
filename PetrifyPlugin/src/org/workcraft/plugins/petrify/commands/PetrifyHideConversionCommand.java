package org.workcraft.plugins.petrify.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyHideConversionCommand extends PetrifyAbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Net synthesis hiding selected signals and dummies [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        VisualModel visualModel = we.getModelEntry().getVisualModel();
        HashSet<VisualTransition> transitions = PetriNetUtils.getVisualTransitions(visualModel);
        transitions.retainAll(visualModel.getSelection());

        HashSet<String> nameSet = new HashSet<>();
        for (VisualTransition transition: transitions) {
            String name = null;
            if (transition instanceof VisualSignalTransition) {
                name = ((VisualSignalTransition) transition).getSignalName();
            } else if (transition instanceof VisualDummyTransition) {
                name = ((VisualDummyTransition) transition).getReferencedTransition().getName();
            } else {
                name = visualModel.getMathName(transition);
            }
            if ((name != null) && !name.isEmpty()) {
                nameSet.add(name);
            }
        }
        ArrayList<String> args = getArgs();
        if (!nameSet.isEmpty()) {
            String names = "";
            for (String name: nameSet) {
                if (!names.isEmpty()) {
                    names += ",";
                }
                names += name;
            }
            args.add("-hide");
            args.add(names);
        }

        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final PetrifyTransformationTask task = new PetrifyTransformationTask(we, "Net synthesis", args.toArray(new String[args.size()]));
        boolean hasSignals = hasSignals(we);
        Collection<Mutex> mutexes = getMutexes(we);
        final PetrifyTransformationResultHandler monitor = new PetrifyTransformationResultHandler(we, !hasSignals, mutexes);
        taskManager.execute(task, "Petrify net synthesis", monitor);
        return monitor.waitForHandledResult();
    }

}
