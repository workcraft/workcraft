package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Undirected;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.util.Hierarchy;

public abstract class AbstractMergerTool extends TransformationTool {

    @Override
    public String getDisplayName() {
        return "Merge selected nodes";
    }

    public Set<Class<? extends VisualComponent>> getMergableClasses() {
        return new HashSet<Class<? extends VisualComponent>>();
    }

    @Override
    public void transform(Model model, Collection<Node> nodes) {
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
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            for (Class<? extends VisualComponent> mergableClass: mergableClasses) {
                Set<VisualComponent> components = classComponents.get(mergableClass);
                VisualComponent mergedComponent = createMergedComponent(visualModel, components, mergableClass);
                replaceComponents(visualModel, components, mergedComponent);
                if (mergedComponent != null) {
                    visualModel.addToSelection(mergedComponent);
                }
            }
        }
    }

    @Override
    public void transform(Model model, Node node) {
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
                MathNode mathNode = mathModel.createNode(nodes, mContainer, mathNodeClass);
                result = model.createVisualComponent(mathNode, vContainer, type);
                int n = components.size();
                result.setRootSpacePosition(new Point2D.Double(x / n, y / n));
                result.mixStyle(components.toArray(new VisualComponent[components.size()]));
            }
        }
        return result;
    }

    public void replaceComponents(VisualModel model, Set<VisualComponent> components, VisualComponent newComponent) {
        for (VisualComponent component: components) {
            for (Connection connection: model.getConnections(component)) {
                boolean isUndirected = connection instanceof Undirected;
                Node first = connection.getFirst();
                Node second = connection.getSecond();
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
                        newConnection.copyStyle((VisualConnection) connection);
                        newConnection.copyShape((VisualConnection) connection);
                    }
                } catch (InvalidConnectionException e) {
                }
            }
            model.remove(component);
        }
    }

}
