package org.workcraft.commands;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Undirected;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.util.*;

public abstract class AbstractMergeTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Merge selected nodes";
    }

    public Set<Class<? extends VisualComponent>> getMergableClasses() {
        return new HashSet<>();
    }

    @Override
    public void transform(VisualModel model, Collection<? extends VisualNode> nodes) {
        Map<Class<? extends VisualComponent>, Set<VisualComponent>> classComponents = new HashMap<>();
        Set<Class<? extends VisualComponent>> mergableClasses = getMergableClasses();
        for (Class<? extends VisualComponent> mergableClass: mergableClasses) {
            Set<VisualComponent> components = new HashSet<>();
            for (Node component: nodes) {
                if (mergableClass.isInstance(component)) {
                    components.add((VisualComponent) component);
                }
            }
            classComponents.put(mergableClass, components);
        }
        for (Class<? extends VisualComponent> mergableClass: mergableClasses) {
            Set<VisualComponent> components = classComponents.get(mergableClass);
            VisualComponent mergedComponent = createMergedComponent(model, components, mergableClass);
            replaceComponents(model, components, mergedComponent);
            if (mergedComponent != null) {
                model.addToSelection(mergedComponent);
            }
        }
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
    }


    public <T extends VisualComponent> T createMergedComponent(VisualModel model, Set<VisualComponent> components, Class<T> type) {
        T result = null;
        if (components != null) {
            double x = 0.0;
            double y = 0.0;
            ArrayList<MathNode> nodes = new ArrayList<>();
            for (VisualComponent component: components) {
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
                result.mixStyle(components.toArray(new VisualComponent[components.size()]));
            }
        }
        return result;
    }

    public void replaceComponents(VisualModel model, Set<VisualComponent> components, VisualComponent newComponent) {
        for (VisualComponent component: components) {
            for (VisualConnection connection: model.getConnections(component)) {
                boolean isUndirected = connection instanceof Undirected;
                VisualNode first = connection.getFirst();
                VisualNode second = connection.getSecond();
                try {
                    VisualConnection newConnection = null;
                    if ((first != component) && (second == component)) {
                        if (isUndirected) {
                            newConnection = model.connectUndirected(first, newComponent);
                        } else {
                            newConnection = model.connect(first, newComponent);
                        }
                    }
                    if ((first == component) && (second != component)) {
                        if (isUndirected) {
                            newConnection = model.connectUndirected(newComponent, second);
                        } else {
                            newConnection = model.connect(newComponent, second);
                        }
                    }
                    if ((newConnection != null) && (connection instanceof VisualConnection)) {
                        newConnection.copyStyle(connection);
                        newConnection.copyShape(connection);
                    }
                } catch (InvalidConnectionException e) {
                }
            }
            model.remove(component);
        }
    }

}
