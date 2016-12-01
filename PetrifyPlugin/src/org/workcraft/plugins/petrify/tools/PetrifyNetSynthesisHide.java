package org.workcraft.plugins.petrify.tools;

import java.util.ArrayList;
import java.util.HashSet;

import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyNetSynthesisHide extends PetrifyNetSynthesis {

    @Override
    public String getDisplayName() {
        return "Net synthesis hiding selected signals and dummies [Petrify]";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, PetriNetModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
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
        final TransformationTask task = new TransformationTask(we, "Net synthesis", args.toArray(new String[args.size()]));
        final Framework framework = Framework.getInstance();
        boolean hasSignals = WorkspaceUtils.isApplicable(we.getModelEntry(), StgModel.class);
        TransformationResultHandler monitor = new TransformationResultHandler(we, hasSignals);
        framework.getTaskManager().queue(task, "Petrify net synthesis", monitor);
    }

}
