package org.workcraft.plugins.dfs.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Container;
import org.workcraft.dom.NodeFactory;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.utils.*;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractInsertTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    // Right arrow symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);

    @Override
    public String getDisplayName() {
        return "Insert " + getTypeName() + " into selected arcs";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Insert " + getTypeName();
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualDfs.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualConnection;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        VisualModel model = me.getVisualModel();
        return (node instanceof VisualConnection) && isValidInsertion(model, (VisualConnection) node);
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<? extends VisualNode> collectNodes(VisualModel model) {
        Collection<VisualConnection> result = new HashSet<>(Hierarchy.getDescendantsOfType(model.getRoot(), VisualConnection.class));
        result.retainAll(model.getSelection());

        Collection<VisualConnection> invalidConnections = result.stream()
                .filter(connection -> !isValidInsertion(model, connection))
                .collect(Collectors.toList());

        result.removeAll(invalidConnections);
        if (!invalidConnections.isEmpty()) {
            String title = "Insertion of " + getTypeName();
            String messagePrefix = "Cannot insert " + getTypeName() + " into ";
            if (result.isEmpty()) {
                String msgSuffix = invalidConnections.size() == 1 ? "selected arc." : "any of the selected arcs.";
                DialogUtils.showError(messagePrefix + msgSuffix);
            } else {
                Collection<String> invalidConnectionRefs = new ArrayList<>();
                for (VisualConnection connection : invalidConnections) {
                    String fromRef = model.getMathReference(connection.getFirst());
                    String toRef = model.getMathReference(connection.getSecond());
                    invalidConnectionRefs.add(fromRef + RIGHT_ARROW_SYMBOL + toRef);
                }
                String messageSuffix = TextUtils.wrapMessageWithItems("arc", invalidConnectionRefs);
                LogUtils.logWarning(messagePrefix + messageSuffix);
                String message = messagePrefix + messageSuffix;
                String question = "\n\nProceed with the other arcs?";
                if (!DialogUtils.showConfirmWarning(message, question, title, false)) {
                    result.clear();
                }
            }
        }
        return result;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if (node instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) node;
            insertWithinConnection(model, connection);
        }
    }

    private boolean isValidInsertion(VisualModel model, VisualConnection connection) {
        try {
            VisualNode node = NodeFactory.createVisualComponent(createComponent());
            VisualNode fromNode = connection.getFirst();
            VisualNode toNode = connection.getSecond();

            model.validateConnection(fromNode, node);
            model.validateConnection(node, toNode);
        } catch (NodeCreationException | InvalidConnectionException e) {
            return false;
        }
        return true;
    }

    private void insertWithinConnection(VisualModel model, VisualConnection connection) {
        VisualComponent node = model.createVisualComponent(createComponent(), VisualComponent.class);
        VisualNode fromNode = connection.getFirst();
        VisualNode toNode = connection.getSecond();
        Container container = Hierarchy.getNearestContainer(fromNode, toNode);

        model.reparent(container, model, model.getRoot(), Collections.singletonList(node));

        Point2D pos = connection.getMiddleSegmentCenterPoint();
        node.setPosition(pos);

        LinkedList<Point2D> prefixControlPoints = ConnectionHelper.getPrefixControlPoints(connection, pos);
        LinkedList<Point2D> suffixControlPoints = ConnectionHelper.getSuffixControlPoints(connection, pos);
        model.remove(connection);
        try {
            VisualConnection inputConnection = model.connect(fromNode, node);
            ConnectionHelper.addControlPoints(inputConnection, prefixControlPoints);
            VisualConnection outputConnection = model.connect(node, toNode);
            ConnectionHelper.addControlPoints(outputConnection, suffixControlPoints);
        } catch (InvalidConnectionException e) {
            LogUtils.logWarning(e.getMessage());
        }
    }

    public abstract String getTypeName();

    public abstract MathNode createComponent();

}
