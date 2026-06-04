package org.workcraft.plugins.stg.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.stg.*;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IncrementSignalTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private static final Pattern PATTERN = Pattern.compile("^(.+)(\\d+)(\\D*)$");
    private static final int PREFIX_GROUP = 1;
    private static final int INFIX_GROUP = 2;
    private static final int SUFFIX_GROUP = 3;

    @Override
    public String getDisplayName() {
        return "Increment last number in signals (selected or all)";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Increment last number in signal name";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualSignalTransition);
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public void transform(WorkspaceEntry we) {
        VisualModel visualModel = WorkspaceUtils.getAs(we, VisualModel.class);
        Collection<? extends VisualNode> nodes = collectNodes(visualModel);
        if (nodes.isEmpty()) {
            logNoNodesWarning(visualModel);
        } else {
            we.captureMemento();
            visualModel.selectNone();
            try {
                transformNodes(visualModel, nodes);
                we.saveMemento();
                we.setChanged(true);
            } catch (ArgumentException e) {
                we.cancelMemento();
                DialogUtils.showError("Cannot perform transformation: " + e.getMessage());
            }
        }
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Set<VisualNode> signalTransitions = new HashSet<>();
        if (model instanceof VisualStg stg) {
            signalTransitions.addAll(stg.getVisualSignalTransitions());
            Collection<VisualNode> selection = stg.getSelection();
            if (!selection.isEmpty()) {
                signalTransitions.retainAll(selection);
            }
        }
        return SortUtils.getSortedNatural(signalTransitions, model::getMathReference);
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualStg visualStg) && (node instanceof VisualSignalTransition visualSignalTransition)) {
            Stg stg = visualStg.getMathModel();
            SignalTransition signalTransition = visualSignalTransition.getReferencedComponent();
            String signalRef = stg.getSignalReference(signalTransition);
            Matcher matcher = PATTERN.matcher(signalRef);
            if (matcher.find()) {
                String prefix = matcher.group(PREFIX_GROUP);
                String infix = matcher.group(INFIX_GROUP);
                String suffix = matcher.group(SUFFIX_GROUP);
                try {
                    int num = Integer.parseInt(infix);
                    String newSignalRef = prefix + (num + 1) + suffix;
                    Node occupantNode = stg.getNodeByReference(newSignalRef);
                    if (occupantNode == null) {
                        Signal.Type signalType = signalTransition.getSignalType();
                        Signal.Type existingSignalType = stg.getSignalType(newSignalRef);
                        if ((existingSignalType != null) && (existingSignalType != signalType)) {
                            LogUtils.logWarning("Transition of " + signalType + " signal '" + signalRef
                                    + "' is converted to " + existingSignalType + " signal '" + newSignalRef + "'");
                        }
                        stg.setName(signalTransition, newSignalRef);
                    } else {
                        throw new ArgumentException("Name '" + newSignalRef + "' is unavailable.");
                    }
                } catch (NumberFormatException ignored) {
                    // Skip signal names without numbers
                }
            }
        }
    }

}
