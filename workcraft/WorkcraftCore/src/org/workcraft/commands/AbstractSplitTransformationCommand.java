package org.workcraft.commands;

import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.ModelUtils;
import org.workcraft.workspace.ModelEntry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractSplitTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private final Set<Class<? extends VisualComponent>> splittableClasses = new HashSet<>();

    @Override
    public String getDisplayName() {
        return "Split selected nodes";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Split node";
    }

    @Override
    public MenuVisibility getMenuVisibility() {
        return MenuVisibility.APPLICABLE_POPUP_ONLY;
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        for (Class<? extends VisualComponent> splitableClass : splittableClasses) {
            if (splitableClass.isInstance(node)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    public void registerSplittableClass(Class<? extends VisualComponent> splitableClass) {
        splittableClasses.add(splitableClass);
    }

    @Override
    public void transformNodes(VisualModel model, Collection<? extends VisualNode> nodes) {
        for (VisualNode node : nodes) {
            if (isApplicableTo(node)) {
                beforeNodeTransformation(model, node);
                transformNode(model, node);
                afterNodeTransformation(model, node);
            }
        }
    }

    public void beforeNodeTransformation(VisualModel model, VisualNode node) {
    }

    public void afterNodeTransformation(VisualModel model, VisualNode node) {
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if (node instanceof VisualComponent component) {
            VisualComponent firstComponent = createDuplicate(model, component);
            VisualComponent secondComponent = createDuplicate(model, component);
            for (Replica replica : component.getReplicas()) {
                if (replica instanceof VisualReplica vReplica) {
                    Class<? extends VisualReplica> replicaClass = vReplica.getClass();
                    Container replicaContainer = Hierarchy.getNearestContainer(vReplica);
                    VisualReplica fromReplica = null;
                    VisualReplica toReplica = null;
                    for (VisualConnection connection : model.getConnections(vReplica)) {
                        VisualNode predNode = connection.getFirst();
                        VisualNode succNode = connection.getSecond();
                        if (predNode == replica) {
                            if (toReplica == null) {
                                toReplica = model.createVisualReplica(secondComponent, replicaClass, replicaContainer);
                                toReplica.copyStyle(vReplica);
                                toReplica.copyPosition(vReplica);
                            }
                            try {
                                VisualConnection newConnection = model.connect(toReplica, succNode);
                                copyConnectionDetails(model, connection, newConnection);
                            } catch (InvalidConnectionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (succNode == replica) {
                            if (fromReplica == null) {
                                fromReplica = model.createVisualReplica(firstComponent, replicaClass, replicaContainer);
                                fromReplica.copyStyle(vReplica);
                                fromReplica.copyPosition(vReplica);
                            }
                            try {
                                VisualConnection newConnection = model.connect(predNode, fromReplica);
                                copyConnectionDetails(model, connection, newConnection);
                            } catch (InvalidConnectionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
            for (VisualConnection connection : model.getConnections(component)) {
                VisualNode predNode = connection.getFirst();
                VisualNode succNode = connection.getSecond();
                if (predNode == component) {
                    try {
                        VisualConnection newConnection = model.connect(secondComponent, succNode);
                        copyConnectionDetails(model, connection, newConnection);
                    } catch (InvalidConnectionException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (succNode == component) {
                    try {
                        VisualConnection newConnection = model.connect(predNode, firstComponent);
                        copyConnectionDetails(model, connection, newConnection);
                    } catch (InvalidConnectionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // Adjust positions of split components
            removeOriginalComponentAndInheritName(model, component, firstComponent, secondComponent);
            adjustSplitComponentPositions(model, firstComponent, secondComponent);
            connectSplitComponents(model, firstComponent, secondComponent);
        }
    }

    public void removeOriginalComponentAndInheritName(VisualModel model, VisualComponent component,
            VisualComponent firstComponent, VisualComponent secondComponent) {

        String desiredName = model.getMathName(component);
        model.remove(component);
        // Rename firstComponent last, so it picks the name of the original component
        ModelUtils.renameNode(model, secondComponent, desiredName);
        ModelUtils.renameNode(model, firstComponent, desiredName);
    }

    private void adjustSplitComponentPositions(VisualModel model,
            VisualComponent firstComponent, VisualComponent secondComponent) {

        Collection<Point2D> incomingGradients = new ArrayList<>();
        for (VisualConnection connection : model.getConnections(firstComponent)) {
            incomingGradients.add(calcConnectionSecondVector(connection));
        }
        Collection<Point2D> outgoingGradients = new ArrayList<>();
        for (VisualConnection connection : model.getConnections(secondComponent)) {
            outgoingGradients.add(calcConnectionFirstVector(connection));
        }

        double offset = getSplitOffset();
        if (outgoingGradients.isEmpty() && incomingGradients.isEmpty()) {
            Point2D firstPos = firstComponent.getRootSpacePosition();
            firstComponent.setRootSpacePosition(new Point2D.Double(firstPos.getX() - offset, firstPos.getY()));
            Point2D secondPos = secondComponent.getRootSpacePosition();
            secondComponent.setRootSpacePosition(new Point2D.Double(secondPos.getX() + offset, secondPos.getY()));
        } else if (outgoingGradients.isEmpty()) {
            adjustPosition(secondComponent, incomingGradients, -2 * offset);
        } else if (incomingGradients.isEmpty()) {
            adjustPosition(firstComponent, outgoingGradients, -2 * offset);
        } else if ((outgoingGradients.size() == 1) && (incomingGradients.size() != 1)) {
            adjustPosition(secondComponent, outgoingGradients, 2 * offset);
        } else if ((incomingGradients.size() == 1) && (outgoingGradients.size() != 1)) {
            adjustPosition(firstComponent, incomingGradients, 2 * offset);
        } else {
            adjustPosition(firstComponent, incomingGradients, offset);
            adjustPosition(secondComponent, outgoingGradients, offset);
        }
    }

    private void adjustPosition(VisualComponent component, Collection<Point2D> gradients, double offset) {
        int count = 0;
        double accX = 0.0;
        double accY = 0.0;
        for (Point2D gradient : gradients) {
            double d = gradient.distance(0.0, 0.0);
            if (d > 0.1) {
                accX += gradient.getX() / d;
                accY += gradient.getY() / d;
                count++;
            }
        }
        double xOffset = (count > 0) ? offset * accX / count : 0.0;
        double yOffset = (count > 0) ? offset * accY / count : 0.0;
        Point2D pos = component.getRootSpacePosition();
        component.setRootSpacePosition(new Point2D.Double(pos.getX() + xOffset, pos.getY() + yOffset));
    }

    private static Point2D calcConnectionFirstVector(VisualConnection connection) {
        Point2D fromPoint = connection.getFirstCenter();
        Point2D toPoint = null;
        ConnectionGraphic graphic = connection.getGraphic();
        if (graphic instanceof Polyline polyline) {
            toPoint = polyline.getControlPointCount() > 0
                    ? polyline.getControlPoint(0).getPosition()
                    : connection.getSecondCenter();

        } else if (graphic instanceof Bezier bezier) {
            toPoint = bezier.getControlPoints().get(0).getPosition();
        }
        if ((fromPoint == null) || (toPoint == null)) {
            return new Point2D.Double(0, 0);
        }
        return new Point2D.Double(toPoint.getX() - fromPoint.getX(), toPoint.getY() - fromPoint.getY());
    }

    private static Point2D calcConnectionSecondVector(VisualConnection connection) {
        Point2D fromPoint = null;
        ConnectionGraphic graphic = connection.getGraphic();
        if (graphic instanceof Polyline polyline) {
            fromPoint = polyline.getControlPointCount() > 0
                    ? polyline.getLastControlPoint().getPosition()
                    : connection.getFirstCenter();

        } else if (graphic instanceof Bezier bezier) {
            fromPoint = bezier.getControlPoints().get(1).getPosition();
        }
        Point2D toPoint = connection.getSecondCenter();
        if ((toPoint == null) || (fromPoint == null)) {
            return new Point2D.Double(0, 0);
        }
        return new Point2D.Double(fromPoint.getX() - toPoint.getX(), fromPoint.getY() - toPoint.getY());
    }

    public double getSplitOffset() {
        return 1.0;
    }

    public VisualComponent createDuplicate(VisualModel model, VisualComponent component) {
        Container vContainer = Hierarchy.getNearestContainer(component);
        Container mContainer = NamespaceHelper.getMathContainer(model, vContainer);
        Class<? extends MathNode> mathNodeClass = component.getReferencedComponent().getClass();
        MathNode mathNode = model.getMathModel().createNode(null, mContainer, mathNodeClass);
        VisualComponent result = model.createVisualComponent(mathNode, component.getClass(), vContainer);
        result.copyStyle(component);
        result.copyPosition(component);
        return result;
    }

    public VisualConnection connectSplitComponents(VisualModel model,
            VisualComponent firstComponent, VisualComponent secondComponent) {

        try {
            return model.connect(firstComponent, secondComponent);
        } catch (InvalidConnectionException e1) {
            throw new RuntimeException(e1);
        }
    }

    public void copyConnectionDetails(VisualModel model, VisualConnection connection, VisualConnection newConnection) {
        newConnection.copyShape(connection);
        newConnection.copyStyle(connection);
    }

}
