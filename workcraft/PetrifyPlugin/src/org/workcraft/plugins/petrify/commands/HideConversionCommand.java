package org.workcraft.plugins.petrify.commands;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.utils.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
    public ArrayList<String> getArgs(WorkspaceEntry we) {
        ArrayList<String> args = super.getArgs(we);

        VisualModel visualModel = we.getModelEntry().getVisualModel();
        Set<VisualTransition> transitions = new HashSet<>(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualTransition.class));
        transitions.retainAll(visualModel.getSelection());

        Set<String> names = new HashSet<>();
        for (VisualTransition transition : transitions) {
            String name;
            if (transition instanceof VisualSignalTransition) {
                name = ((VisualSignalTransition) transition).getSignalName();
            } else if (transition instanceof VisualDummyTransition) {
                name = ((VisualDummyTransition) transition).getReferencedComponent().getName();
            } else {
                name = visualModel.getMathName(transition);
            }
            if ((name != null) && !name.isEmpty()) {
                names.add(name);
            }
        }

        if (!names.isEmpty()) {
            args.add("-hide");
            args.add(String.join(",", names));
        }
        return args;
    }

}
