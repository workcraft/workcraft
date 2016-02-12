package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Undirected;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

abstract public class AbstractMergerTool extends TransformationTool {

    @Override
    public String getDisplayName() {
        return "Merge selected nodes";
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualModel model = we.getModelEntry().getVisualModel();
        if (model.getSelection().size() > 0) {
            we.saveMemento();
            mergeSelection(model);
        }
    }

    public Set<Class<? extends VisualComponent>> getMergableClasses() {
        Set<Class<? extends VisualComponent>> result = new HashSet<>();
        return result;
    }

    private void mergeSelection(VisualModel model) {
        Map<Class<? extends VisualComponent>, Set<VisualComponent>> classComponents = new HashMap<>();
        Set<Class<? extends VisualComponent>> mergableClasses = getMergableClasses();
        for (Class<? extends VisualComponent> mergableClass: mergableClasses) {
            Set<VisualComponent> components = new HashSet<>();
            for (Node component: model.getSelection()) {
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

    public <T extends VisualComponent> T createMergedComponent(VisualModel model, Set<VisualComponent> components, Class<T> type) {
        T result = null;
        if (components != null) {
            double x = 0.0;
            double y = 0.0;
            for (VisualComponent component: components) {
                x += component.getRootSpaceX();
                y += component.getRootSpaceY();
            }
            if (!components.isEmpty()) {
                Container vContainer = Hierarchy.getNearestContainer(new ArrayList<Node>(components));
                Container mContainer = NamespaceHelper.getMathContainer(model, vContainer);
                Class<? extends MathNode> mathNodeClass = components.iterator().next().getReferencedComponent().getClass();
                MathNode mathNode = model.getMathModel().createNode(null, mContainer, mathNodeClass);
                result = model.createVisualComponent(mathNode, vContainer, type);
                int n = components.size();
                result.setRootSpacePosition(new Point2D.Double(x / n, y/ n));
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
