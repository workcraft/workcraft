package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandlingMonitor;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class HideConversionCommand extends AbstractConversionCommand {

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
        HashSet<VisualTransition> transitions = ConversionUtils.getVisualTransitions(visualModel);
        transitions.retainAll(visualModel.getSelection());

        HashSet<String> nameSet = new HashSet<>();
        for (VisualTransition transition: transitions) {
            String name = null;
            if (transition instanceof VisualSignalTransition) {
                name = ((VisualSignalTransition) transition).getSignalName();
            } else if (transition instanceof VisualDummyTransition) {
                name = ((VisualDummyTransition) transition).getReferencedComponent().getName();
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

        Collection<Mutex> mutexes = getMutexes(we);
        TransformationTask task = new TransformationTask(we, args, mutexes);

        boolean hasSignals = hasSignals(we);
        TransformationResultHandlingMonitor monitor = new TransformationResultHandlingMonitor(we, !hasSignals, mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        taskManager.execute(task, "Petrify net synthesis", monitor);
        return monitor.waitForHandledResult();
    }

}
