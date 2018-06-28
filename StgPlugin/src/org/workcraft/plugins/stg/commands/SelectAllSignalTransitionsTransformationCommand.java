package org.workcraft.plugins.stg.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.*;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class SelectAllSignalTransitionsTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Select all transitions of selected signals";
    }

    @Override
    public String getPopupName() {
        return "Select all transitions of signal";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualSignalTransition;
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> signalTransitions = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            signalTransitions.addAll(stg.getVisualSignalTransitions());
            signalTransitions.retainAll(stg.getSelection());
        }
        return signalTransitions;
    }

    @Override
    public void transform(Model model, Collection<Node> nodes) {
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            HashSet<String> signals = new HashSet<>();
            for (Node node: nodes) {
                if (node instanceof VisualSignalTransition) {
                    VisualSignalTransition st = (VisualSignalTransition) node;
                    signals.add(stg.getSignalReference(st));
                }
            }
            Stg mathStg = (Stg) stg.getMathModel();
            stg.selectNone();
            for (String signal: signals) {
                for (SignalTransition mathTransition: mathStg.getSignalTransitions(signal)) {
                    stg.addToSelection(stg.getVisualComponent(mathTransition, VisualSignalTransition.class));
                }
            }
        }
    }

    @Override
    public void transform(Model model, Node node) {
    }

}
