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
            Set<VisualComponent> components = new HashSet<>();
            for (Node component : nodes) {
                if (mergableClass.isInstance(component)) {
                    components.add((VisualComponent) component);
                }
            }
            classComponents.put(mergableClass, components);
        }
        for (Class<? extends VisualComponent> mergableClass : mergableClasses) {
            Set<VisualComponent> components = classComponents.get(mergableClass);
            if (components.size() > 1) {
                VisualComponent mergedComponent = createMergedComponent(model, components, mergableClass);
                replaceComponents(model, components, mergedComponent);
                if (mergedComponent != null) {
                    model.addToSelection(mergedComponent);
                }
            }
        }
    }

    public <T extends VisualComponent> T createMergedComponent(VisualModel model, Set<VisualComponent> components, Class<T> type) {
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
                Class<? extends MathNode> mathNodeClass = components.iterator().next().getReferencedComponent().getClass();
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

    public void replaceComponents(VisualModel model, Set<VisualComponent> components,
            VisualComponent newComponent) {

        for (VisualComponent component : components) {
            for (Replica replica : component.getReplicas()) {
                if (replica instanceof VisualReplica componentReplica) {
                    Container container = Hierarchy.getNearestContainer(componentReplica);
                    VisualReplica newComponentReplica = model.createVisualReplica(newComponent, componentReplica.getClass(), container);
                    newComponentReplica.copyStyle(componentReplica);
                    newComponentReplica.copyPosition(componentReplica);
                    for (VisualConnection connection : model.getConnections(componentReplica)) {
                        createMergedConnection(model, connection, componentReplica, newComponentReplica);
                    }
                }
            }
            for (VisualConnection connection : model.getConnections(component)) {
                createMergedConnection(model, connection, component, newComponent);
            }
            model.remove(component);
        }
    }

    public VisualConnection createMergedConnection(VisualModel model, VisualConnection connection,
            VisualNode component, VisualNode newComponent) {

        boolean isUndirected = connection instanceof Undirected;
        VisualNode first = connection.getFirst();
        VisualNode second = connection.getSecond();
        VisualConnection newConnection = null;
        if ((first != component) && (second == component)) {
            newConnection = createConnection(model, first, newComponent, isUndirected);
        }
        if ((first == component) && (second != component)) {
            newConnection = createConnection(model, newComponent, second, isUndirected);
        }
        if ((first == component) && (second == component)) {
            newConnection = createConnection(model, newComponent, newComponent, isUndirected);
        }

        if (newConnection != null) {
            newConnection.copyStyle(connection);
            if (newConnection.getFirst() == newConnection.getSecond()) {
                newConnection.getGraphic().setDefaultControlPoints();
            } else {
                newConnection.copyShape(connection);
            }
        }
        return newConnection;
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
