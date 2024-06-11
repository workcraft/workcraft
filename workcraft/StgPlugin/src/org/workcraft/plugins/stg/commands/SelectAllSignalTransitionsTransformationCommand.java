package org.workcraft.plugins.stg.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

public class SelectAllSignalTransitionsTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Select all transitions of selected signals";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Select all transitions of signal";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualSignalTransition;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
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
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> signalTransitions = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            signalTransitions.addAll(stg.getVisualSignalTransitions());
            signalTransitions.retainAll(stg.getSelection());
        }
        return signalTransitions;
    }

    @Override
    public void transformNodes(VisualModel model, Collection<? extends VisualNode> nodes) {
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            HashSet<String> signals = new HashSet<>();
            for (VisualNode node: nodes) {
                if (node instanceof VisualSignalTransition) {
                    VisualSignalTransition st = (VisualSignalTransition) node;
                    signals.add(stg.getSignalReference(st));
                }
            }
            Stg mathStg = stg.getMathModel();
            stg.selectNone();
            for (String signal: signals) {
                for (SignalTransition mathTransition: mathStg.getSignalTransitions(signal)) {
                    stg.addToSelection(stg.getVisualComponent(mathTransition, VisualSignalTransition.class));
                }
            }
        }
    }

}
