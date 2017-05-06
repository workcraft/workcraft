package org.workcraft.plugins.stg.commands;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.NodeTransformer;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.commands.AbstractTransformationCommand;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.util.Pair;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ExpandHandshakeTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private static final String SUFFIX_REQ = "_req";
    private static final String SUFFIX_ACK = "_ack";
    private HashSet<Node> expandedNodes = null;
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
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualSignalTransition;
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return null;
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
            expandedNodes = new HashSet<Node>();
            suffixPair = getSufixes();
            for (Node node: nodes) {
                transform(model, node);
            }
            stg.select(expandedNodes);
            expandedNodes = null;
            suffixPair = null;
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualStg) && (node instanceof VisualSignalTransition)) {
            VisualStg stg = (VisualStg) model;
            VisualSignalTransition transition = (VisualSignalTransition) node;
            String ref = stg.getSignalReference(transition);
            Type type = transition.getSignalType();
            Direction direction = transition.getDirection();
            Container container = Hierarchy.getNearestContainer(transition);
            String reqSuffix = SUFFIX_REQ;
            String ackSuffix = SUFFIX_ACK;
            if (suffixPair != null) {
                reqSuffix = suffixPair.getFirst();
                ackSuffix = suffixPair.getSecond();
            }
            VisualSignalTransition reqTransition = stg.createVisualSignalTransition(ref + reqSuffix, type, direction, container);
            if (type == Type.INPUT) {
                type = Type.OUTPUT;
            } else if (type == Type.OUTPUT) {
                type = Type.INPUT;
            }
            VisualSignalTransition ackTransition = stg.createVisualSignalTransition(ref + ackSuffix, type, direction, container);
            Point2D pos = transition.getRootSpacePosition();
            reqTransition.setRootSpacePosition(new Point2D.Double(pos.getX() - 2.0, pos.getY()));
            ackTransition.setRootSpacePosition(new Point2D.Double(pos.getX() + 2.0, pos.getY()));
            VisualConnection midConnection = null;
            try {
                midConnection = stg.connect(reqTransition, ackTransition);
                for (Connection connection: stg.getConnections(transition)) {
                    Node predNode = connection.getFirst();
                    Node succNode = connection.getSecond();
                    if (connection instanceof VisualReadArc) {
                        String predRef = stg.getNodeMathReference(predNode);
                        String succRef = stg.getNodeMathReference(succNode);
                        LogUtils.logWarningLine("Read-arc between '" + predRef + "' and '" + succRef + "' is ignored.");
                        continue;
                    }
                    if (transition == succNode) {
                        VisualConnection predConnection = stg.connect(predNode, reqTransition);
                        predConnection.copyShape((VisualConnection) connection);
                        predConnection.copyStyle((VisualConnection) connection);
                    }
                    if (transition == predNode) {
                        VisualConnection succConnection = stg.connect(ackTransition, succNode);
                        succConnection.copyStyle((VisualConnection) connection);
                        succConnection.copyShape((VisualConnection) connection);
                    }
                }
            } catch (InvalidConnectionException e) {
            }
            if (expandedNodes == null) {
                expandedNodes.add(reqTransition);
                expandedNodes.add(ackTransition);
                if (midConnection != null) {
                    expandedNodes.add(midConnection);
                }
            }
            stg.remove(transition);
        }
    }

    public Pair<String, String> getSufixes() {
        Pair<String, String> result = null;
        final Framework framework = Framework.getInstance();
        String ans = JOptionPane.showInputDialog(framework.getMainWindow(),
                "Enter a pair of space-separated suffixes for handshake signals:",
                SUFFIX_REQ + " " + SUFFIX_ACK);
        if (ans != null) {
            String[] split = ans.trim().split("\\s");
            if (split.length == 2) {
                result = Pair.of(split[0], split[1]);
            } else {
                JOptionPane.showMessageDialog(framework.getMainWindow(),
                        "Error: Two suffixes are required!\n\n" +
                        "Default suffixes " + SUFFIX_REQ + " and " + SUFFIX_ACK + " will be used.",
                        "Handshake expansion", JOptionPane.ERROR_MESSAGE);
                result = null;
            }
        }
        return result;
    }

}
