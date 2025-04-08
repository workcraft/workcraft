package org.workcraft.commands;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.util.*;

public abstract class AbstractMergeTransformationCommand extends AbstractTransformationCommand {

    private final Set<Class<? extends VisualComponent>> mergableClasses = new HashSet<>();

    @Override
    public String getDisplayName() {
        return "Merge selected nodes";
    }

    public void registerMergableClass(Class<? extends VisualComponent> mergableClass) {
        mergableClasses.add(mergableClass);
    }

    @Override
    public void transformNodes(VisualModel model, Collection<? extends VisualNode> nodes) {
        Map<Class<? extends VisualComponent>, Set<VisualComponent>> classComponents = new HashMap<>();
        for (Class<? extends VisualComponent> mergableClass : mergableClasses) {
            Set<VisualComponent> components = calcMergableComponents(nodes, mergableClass);
            classComponents.put(mergableClass, components);
        }
        for (Class<? extends VisualComponent> mergableClass : mergableClasses) {
            Set<VisualComponent> components = classComponents.get(mergableClass);
            if (components.size() > 1) {
                VisualComponent mergedComponent = createMergedComponent(model, components, mergableClass);
                if (mergedComponent != null) {
                    replaceComponents(model, components, mergedComponent);
                    model.addToSelection(mergedComponent);
                }
            }
        }
    }

    private static Set<VisualComponent> calcMergableComponents(Collection<? extends VisualNode> nodes,
            Class<? extends VisualComponent> mergableClass) {

        Set<VisualComponent> components = new HashSet<>();
        for (Node node : nodes) {
            // Add node if it is of mergable class
            if (mergableClass.isInstance(node)) {
                VisualComponent component = (VisualComponent) node;
                components.add(component);
            }
            // Add replica node master if that is of mergable class
            if (node instanceof Replica replica) {
                VisualComponent masterComponent = replica.getMaster();
                if (mergableClass.isInstance(masterComponent)) {
                    components.add(masterComponent);
                }
            }
        }
        return components;
    }

    public <T extends VisualComponent> T createMergedComponent(VisualModel model,
            Collection<VisualComponent> components, Class<T> type) {

        T result = null;
        if (components != null) {
            double x = 0.0;
            double y = 0.0;
            ArrayList<MathNode> nodes = new ArrayList<>();
            for (VisualComponent component : components) {
                x += component.getRootSpaceX();
                y += component.getRootSpaceY();
                nodes.add(component.getReferencedComponent());
            }
            if (!components.isEmpty()) {
                Container vContainer = Hierarchy.getNearestContainer(new ArrayList<Node>(components));
                Container mContainer = NamespaceHelper.getMathContainer(model, vContainer);
                VisualComponent component = components.iterator().next();
                Class<? extends MathNode> mathNodeClass = component.getReferencedComponent().getClass();
                MathModel mathModel = model.getMathModel();
                MathNode mathNode = mathModel.createMergedNode(nodes, mContainer, mathNodeClass);
                result = model.createVisualComponent(mathNode, type, vContainer);
                int n = components.size();
                result.setRootSpacePosition(new Point2D.Double(x / n, y / n));
                result.mixStyle(components.toArray(new VisualComponent[0]));
            }
        }
        return result;
    }

    public void replaceComponents(VisualModel model, Collection<VisualComponent> components,
            VisualComponent mergedComponent) {

        // First, create all connections to/from non-proxies
        for (VisualComponent component : components) {
            for (VisualConnection connection : model.getConnections(component)) {
                if (!(connection.getFirst() instanceof Replica) && !(connection.getSecond() instanceof Replica)) {
                    createMergedConnection(model, connection, component, mergedComponent);
                }
            }
        }
        // Then, create all connections to/from proxies
        for (VisualComponent component : components) {
            for (VisualConnection connection : model.getConnections(component)) {
                if ((connection.getFirst() instanceof Replica) || (connection.getSecond() instanceof Replica)) {
                    createMergedConnection(model, connection, component, mergedComponent);
                }
            }
        }
        // Finally, create connections to/from proxies of the nodes
        for (VisualComponent component : components) {
            for (Replica replica : component.getReplicas()) {
                if (replica instanceof VisualReplica componentReplica) {
                    Set<VisualConnection> replicaConnections = model.getConnections(componentReplica);
                    Container container = Hierarchy.getNearestContainer(componentReplica);
                    VisualReplica mergedComponentReplica = model.createVisualReplica(mergedComponent,
                            componentReplica.getClass(), container);

                    mergedComponentReplica.copyStyle(componentReplica);
                    mergedComponentReplica.copyPosition(componentReplica);
                    Set<VisualConnection> mergedReplicaConnections = new HashSet<>();
                    for (VisualConnection replicaConnection : replicaConnections) {
                        VisualConnection mergedReplicaConnection = createMergedConnection(model,
                                replicaConnection, componentReplica, mergedComponentReplica);

                        if (mergedReplicaConnection != null) {
                            mergedReplicaConnections.add(mergedReplicaConnection);
                        }
                    }
                    if (mergedReplicaConnections.isEmpty()) {
                        mergedComponent.removeReplica(mergedComponentReplica);
                        model.remove(mergedComponentReplica);
                    }
                }
            }
        }
        // Remove original components after all connections to their merged counterpart are created
        model.remove(components);
    }

    public VisualConnection createMergedConnection(VisualModel model, VisualConnection connection,
            VisualNode node, VisualNode mergedNode) {

        boolean isUndirected = connection instanceof Undirected;
        VisualNode firstNode = connection.getFirst();
        VisualNode secondNode = connection.getSecond();
        VisualConnection mergedConnection = null;
        if ((firstNode != node) && (secondNode == node)) {
            mergedConnection = createConnection(model, firstNode, mergedNode, isUndirected);
        }
        if ((firstNode == node) && (secondNode != node)) {
            mergedConnection = createConnection(model, mergedNode, secondNode, isUndirected);
        }
        if ((firstNode == node) && (secondNode == node)) {
            mergedConnection = createConnection(model, mergedNode, mergedNode, isUndirected);
        }

        if (mergedConnection != null) {
            mergedConnection.copyStyle(connection);
            if (mergedConnection.getFirst() == mergedConnection.getSecond()) {
                mergedConnection.getGraphic().setDefaultControlPoints();
            } else {
                mergedConnection.copyShape(connection);
            }
        }
        return mergedConnection;
    }

    private VisualConnection createConnection(VisualModel model, VisualNode first,
            VisualNode second, boolean isUndirected) {

        try {
            return isUndirected ? model.connectUndirected(first, second)
                    : model.connect(first, second);
        } catch (InvalidConnectionException e) {
            return null;
        }
    }

}
