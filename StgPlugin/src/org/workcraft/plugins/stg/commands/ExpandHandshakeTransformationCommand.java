package org.workcraft.plugins.stg.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.MixUtils;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.util.Pair;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class ExpandHandshakeTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private static final String SUFFIX_REQ = "_req";
    private static final String SUFFIX_ACK = "_ack";
    private Pair<String, String> suffixPair = null;

    @Override
    public String getDisplayName() {
        return "Expand selected handshake transitions...";
    }

    @Override
    public String getPopupName() {
        return "Expand handshake transition...";
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
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

    @Override
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> signalTransitions = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            signalTransitions.addAll(stg.getVisualSignalTransitions());
            signalTransitions.retainAll(stg.getSelection());
        }
        return signalTransitions;
    }

    @Override
    public void transform(VisualModel model, Collection<? extends VisualNode> nodes) {
        if (model instanceof VisualStg) {
            suffixPair = getSufixes();
            if (suffixPair != null) {
                for (VisualNode node: nodes) {
                    transform(model, node);
                }
            }
            suffixPair = null;
        }
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if ((model instanceof VisualStg) && (node instanceof VisualSignalTransition)) {
            VisualStg stg = (VisualStg) model;
            VisualSignalTransition transition = (VisualSignalTransition) node;
            String ref = stg.getSignalReference(transition);
            SignalTransition.Direction direction = transition.getDirection();
            Container container = Hierarchy.getNearestContainer(transition);
            String reqSuffix = SUFFIX_REQ;
            String ackSuffix = SUFFIX_ACK;
            if (suffixPair != null) {
                reqSuffix = suffixPair.getFirst();
                ackSuffix = suffixPair.getSecond();
            }
            VisualSignalTransition reqTransition = stg.createVisualSignalTransition(ref + reqSuffix, Signal.Type.OUTPUT, direction, container);
            VisualSignalTransition ackTransition = stg.createVisualSignalTransition(ref + ackSuffix, Signal.Type.INPUT, direction, container);
            Pair<Point2D, Point2D> positionPair = getReqAckPositions(stg, transition);
            reqTransition.setRootSpacePosition(positionPair.getFirst());
            ackTransition.setRootSpacePosition(positionPair.getSecond());
            VisualConnection midConnection = null;
            try {
                midConnection = stg.connect(reqTransition, ackTransition);
                for (VisualConnection connection: stg.getConnections(transition)) {
                    VisualNode predNode = connection.getFirst();
                    VisualNode succNode = connection.getSecond();
                    if (connection instanceof VisualReadArc) {
                        String predRef = stg.getMathReference(predNode);
                        String succRef = stg.getMathReference(succNode);
                        LogUtils.logWarning("Read-arc between '" + predRef + "' and '" + succRef + "' is ignored.");
                        continue;
                    }
                    if (transition == succNode) {
                        VisualConnection predConnection = stg.connect(predNode, reqTransition);
                        predConnection.copyShape(connection);
                        predConnection.copyStyle(connection);
                    }
                    if (transition == predNode) {
                        VisualConnection succConnection = stg.connect(ackTransition, succNode);
                        succConnection.copyStyle(connection);
                        succConnection.copyShape(connection);
                    }
                }
            } catch (InvalidConnectionException e) {
            }
            model.addToSelection(reqTransition);
            model.addToSelection(ackTransition);
            if (midConnection != null) {
                model.addToSelection(midConnection);
            }
            stg.remove(transition);
        }
    }

    private Pair<Point2D, Point2D> getReqAckPositions(VisualStg stg, VisualSignalTransition transition) {
        LinkedList<Point2D> predPoints = new LinkedList<>();
        LinkedList<Point2D> succPoints = new LinkedList<>();
        for (VisualConnection connection: stg.getConnections(transition)) {
            AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);
            if (connection.getFirst() == transition) {
                Point2D posInLocalSpace = connection.getPointOnConnection(1.0 / 3.0);
                Point2D posInRootSpace = localToRootTransform.transform(posInLocalSpace, null);
                succPoints.add(posInRootSpace);
            }
            if (connection.getSecond() == transition) {
                Point2D posInLocalSpace = connection.getPointOnConnection(2.0 / 3.0);
                Point2D posInRootSpace = localToRootTransform.transform(posInLocalSpace, null);
                predPoints.add(posInRootSpace);
            }
        }
        Point2D pos = transition.getRootSpacePosition();
        if (predPoints.isEmpty() && succPoints.isEmpty()) {
            predPoints.add(new Point2D.Double(pos.getX(), pos.getY() - 1.0));
            succPoints.add(new Point2D.Double(pos.getX(), pos.getY() + 1.0));
        } else if (predPoints.isEmpty()) {
            Point2D succPoint = MixUtils.middlePoint(succPoints);
            predPoints.add(new Point2D.Double(2.0 * pos.getX() - succPoint.getX(), 2.0 * pos.getY() - succPoint.getY()));
        }  else if (succPoints.isEmpty()) {
            Point2D predPoint = MixUtils.middlePoint(predPoints);
            succPoints.add(new Point2D.Double(2.0 * pos.getX() - predPoint.getX(), 2.0 * pos.getY() - predPoint.getY()));
        }
        Point2D predPoint = MixUtils.middlePoint(predPoints);
        Point2D succPoint = MixUtils.middlePoint(succPoints);
        return new Pair<>(predPoint, succPoint);
    }

    public Pair<String, String> getSufixes() {
        Pair<String, String> result = null;
        String ans = DialogUtils.showInput("Enter a pair of space-separated suffixes for handshake signals:",
                SUFFIX_REQ + " " + SUFFIX_ACK);
        if (ans != null) {
            String[] split = ans.trim().split("\\s");
            if (split.length == 2) {
                result = Pair.of(split[0], split[1]);
            } else {
                DialogUtils.showError("Two suffixes are required for handshake expansion.\n\n" +
                        "Default suffixes " + SUFFIX_REQ + " and " + SUFFIX_ACK + " will be used.");
                result = null;
            }
        }
        return result;
    }

}
