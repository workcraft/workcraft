/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.workcraft.NodeFactory;
import org.workcraft.annotations.MouseListeners;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.DefaultMathNodeRemover;
import org.workcraft.dom.DefaultReplicaRemover;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.connections.DefaultAnchorGenerator;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.TitlePropertyDescriptor;
import org.workcraft.observation.ModelModifiedEvent;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.layout.AbstractLayoutTool;
import org.workcraft.plugins.layout.DotLayoutTool;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;

@MouseListeners ({ DefaultAnchorGenerator.class })
public abstract class AbstractVisualModel extends AbstractModel implements VisualModel {
    private MathModel mathModel;
    private Container currentLevel;
    private Set<Node> selection = new HashSet<Node>();
    private ObservableStateImpl observableState = new ObservableStateImpl();

    private VisualNode templateNode = null;

    public AbstractVisualModel() {
        this(null, null);
    }

    public AbstractVisualModel(MathModel mathModel) {
        this(mathModel, null);
    }

    public AbstractVisualModel(MathModel mathModel, VisualGroup root) {
        super((root == null) ? new VisualGroup() : root);
        this.mathModel = mathModel;
        this.currentLevel = (VisualGroup)getRoot();
        new TransformEventPropagator().attach(getRoot());
        new SelectionEventPropagator(this).attach(getRoot());
        new RemovedNodeDeselector(this).attach(getRoot());
        new DefaultHangingConnectionRemover(this).attach(getRoot());
        new DefaultMathNodeRemover().attach(getRoot());
        new DefaultReplicaRemover(this).attach(getRoot());

        new StateSupervisor() {
            @Override
            public void handleEvent(StateEvent e) {
                observableState.sendNotification(new ModelModifiedEvent(AbstractVisualModel.this));
            }
        }.attach(getRoot());
    }

    @Override
    public void createDefaultFlatStructure() throws NodeCreationException {
        HashMap <MathNode, VisualComponent> createdNodes = new HashMap<>();
        // Create components
        Queue<Pair<Container, Container>> containerQueue = new LinkedList<>();
        containerQueue.add(new Pair<Container, Container>(getMathModel().getRoot(), getRoot()));
        while (!containerQueue.isEmpty()) {
            Pair<Container, Container> container = containerQueue.remove();
            Container mathContainer = container.getFirst();
            Container visualContainer = container.getSecond();
            for (Node node : mathContainer.getChildren()) {
                if (node instanceof MathConnection) continue;
                if (node instanceof MathNode) {
                    MathNode mathNode = (MathNode)node;
                    VisualComponent visualComponent = (VisualComponent)NodeFactory.createVisualComponent(mathNode);
                    if (visualComponent != null) {
                        visualContainer.add(visualComponent);
                        createdNodes.put(mathNode, visualComponent);
                    }
                    if ((mathNode instanceof Container) && (visualComponent instanceof Container)) {
                        containerQueue.add(new Pair<Container, Container>((Container)mathNode, (Container)visualComponent));
                    }
                }
            }
        }
        // Create connections
        containerQueue.add(new Pair<Container, Container>(getMathModel().getRoot(), getRoot()));
        while (!containerQueue.isEmpty()) {
            Pair<Container, Container> container = containerQueue.remove();
            Container mathContainer = container.getFirst();
            for (Node node : mathContainer.getChildren()) {
                if (node instanceof MathConnection) {
                    MathConnection mathConnection = (MathConnection)node;
                    VisualComponent firstComponent = createdNodes.get(mathConnection.getFirst());
                    VisualComponent secondComponent = createdNodes.get(mathConnection.getSecond());
                    try {
                        connect(firstComponent, secondComponent, mathConnection);
                    } catch (InvalidConnectionException e) {
                    }
                } else if (node instanceof MathNode) {
                    MathNode mathNode = (MathNode)node;
                    VisualComponent visualComponent = createdNodes.get(mathNode);
                    if ((mathNode instanceof Container) && (visualComponent instanceof Container)) {
                        containerQueue.add(new Pair<Container, Container>((Container)mathNode, (Container)visualComponent));
                    }
                }
            }
        }
    }

    @Override
    public VisualConnection connect(Node first, Node second) throws InvalidConnectionException {
        return connect(first, second, null);
    }

    @Override
    public void validateUndirectedConnection(Node first, Node second) throws InvalidConnectionException {
        validateConnection(first, second);
    }

    @Override
    public VisualConnection connectUndirected(Node first, Node second) throws InvalidConnectionException {
        return connect(first, second, null);
    }

    public boolean hasMathConnection(Node first, Node second) {
        MathNode mFirst = getMathReference(first);
        MathNode mSecond = getMathReference(second);
        return getMathModel().hasConnection(mFirst, mSecond);
    }


    public MathNode getMathReference(Node node) {
        VisualComponent component = null;
        if (node instanceof VisualComponent) {
            component = (VisualComponent)node;
        } else if (node instanceof VisualReplica) {
            component = ((VisualReplica)node).getMaster();
        }
        return (component == null) ? null : component.getReferencedComponent();
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends VisualComponent> T createVisualComponent(MathNode mathNode, Container container, Class<T> type) {
        if (container == null) {
            container = getRoot();
        }
        VisualComponent component = null;
        try {
            component = NodeFactory.createVisualComponent(mathNode);
            container.add(component);
        } catch (NodeCreationException e) {
            String mathName = getMathName(mathNode);
            throw new RuntimeException("Cannot create visual component for math node '" + mathName + "' of class '" + type +"'");
        }
        return (T)component;
    }

    @Override
    public <T extends VisualComponent> T getVisualComponent(MathNode mathNode, Class<T> type) {
        T result = null;
        if (mathNode != null) {
            Collection<T> visualComponents = Hierarchy.getDescendantsOfType(getRoot(), type);
            for (T visualComponent: visualComponents) {
                if (visualComponent.getReferencedComponent() == mathNode) {
                    result = visualComponent;
                    break;
                }
            }
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends VisualReplica> T createVisualReplica(VisualComponent masterComponent, Container container, Class<T> type) {
        T replica = null;
        try {
            replica = NodeFactory.createNode(type);
        } catch (NodeCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (replica != null) {
            if (container == null) {
                container = getRoot();
            }
            container.add(replica);
            replica.setMaster(masterComponent);
        }
        return replica;
    }

    @Override
    public void draw(Graphics2D g, Decorator decorator) {
        DrawMan.draw(this, g, decorator, getRoot());
    }

    /**
     * Get the list of selected objects. Returned list is modifiable!
     * @return the selection.
     */
    public Set<Node> selection() {
        return selection;
    }

    private Collection<Node> saveSelection() {
        Set<Node> prevSelection = new HashSet<Node>();
        prevSelection.addAll(selection);
        return prevSelection;
    }

    private void notifySelectionChanged(Collection<Node> prevSelection) {
        sendNotification(new SelectionChangedEvent(this, prevSelection));
    }

    /**
     * Select all components, connections and groups from the <code>root</code> group.
     */
    @Override
    public void selectAll() {
        if(selection.size() == getCurrentLevel().getChildren().size()) {
            return;
        }
        Collection<Node> s = saveSelection();
        selection.clear();
        selection.addAll(getCurrentLevel().getChildren());
        notifySelectionChanged(s);
    }

    /**
     * Clear selection.
     */
    @Override
    public void selectNone() {
        if (!selection.isEmpty()) {
            Collection<Node> s = saveSelection();
            selection.clear();
            notifySelectionChanged(s);
        }
    }

    /**
     * Invert selection.
     */
    @Override
    public void selectInverse() {
        Collection<Node> s = saveSelection();
        selection.clear();
        for (Node node: getCurrentLevel().getChildren()) {
            if (!s.contains(node)) {
                selection.add(node);
            }
        }
        notifySelectionChanged(getCurrentLevel().getChildren());
    }

    private void validateSelection(Node node) {
        if (!Hierarchy.isDescendant(node, getCurrentLevel())) {
            throw new RuntimeException(
                "Cannot select a node that is not in the current editing level ("
                + node + "), parent (" + node.getParent() +")");
        }
    }

    private void validateSelection(Collection<Node> nodes) {
        for (Node node : nodes)
            validateSelection(node);
    }

    public boolean isSelected(Node node) {
        return selection.contains(node);
    }

    public void select(Collection<Node> nodes) {
        if (nodes.isEmpty()) {
            selectNone();
            return;
        }
        Collection<Node> s = saveSelection();
        validateSelection(nodes);
        selection.clear();
        selection.addAll(nodes);
        notifySelectionChanged(s);
    }

    @Override
    public void select(Node node) {
        if (selection.size() == 1 && selection.contains(node)) {
            return;
        }
        Collection<Node> s = saveSelection();
        validateSelection(node);
        selection.clear();
        selection.add(node);
        notifySelectionChanged(s);
    }

    @Override
    public void addToSelection(Node node) {
        if (selection.contains(node)) {
            return;
        }
        Collection<Node> s = saveSelection();
        validateSelection(node);
        selection.add(node);
        notifySelectionChanged(s);
    }

    @Override
    public void addToSelection(Collection<Node> nodes) {
        Collection<Node> s = saveSelection();
        validateSelection(nodes);
        selection.addAll(nodes);
        if (s.size() != selection.size()) {
            notifySelectionChanged(s);
        }
    }

    @Override
    public void removeFromSelection(Node node) {
        if (selection.contains(node)) {
            Collection<Node> s = saveSelection();
            selection.remove(node);
            notifySelectionChanged(s);
        }
    }

    @Override
    public void removeFromSelection(Collection<Node> nodes) {
        Collection<Node> s = saveSelection();
        selection.removeAll(nodes);
        if (s.size() != selection.size()) {
            notifySelectionChanged(s);
        }
    }

    @Override
    public MathModel getMathModel() {
        return mathModel;
    }

    @Override
    public String getNodeMathReference(Node node) {
        if (node instanceof VisualComponent) {
            VisualComponent component = (VisualComponent)node;
            node = component.getReferencedComponent();
        }
        return getMathModel().getNodeReference(node);
    }

    @Override
    public String getMathName(Node node) {
        if (node instanceof VisualComponent) {
            VisualComponent component = (VisualComponent)node;
            node = component.getReferencedComponent();
        }
        return getMathModel().getName(node);
    }

    @Override
    public void setMathName(Node node, String name) {
        if (node instanceof VisualComponent) {
            VisualComponent component = (VisualComponent)node;
            node = component.getReferencedComponent();
        }
        getMathModel().setName(node, name);
    }

    @Override
    public Container getCurrentLevel() {
        return currentLevel;
    }

    @Override
    public void setCurrentLevel(Container newCurrentLevel) {
        selection.clear();
        currentLevel = newCurrentLevel;

        // manage the isInside value for all parents and children
        Collapsible collapsible = null;
        if (newCurrentLevel instanceof Collapsible) {
            collapsible = (Collapsible)newCurrentLevel;
        }

        if (collapsible != null) {
            collapsible.setIsCurrentLevelInside(true);
            Node parent = newCurrentLevel.getParent();
            while (parent != null) {
                if (parent instanceof Collapsible) {
                    ((Collapsible)parent).setIsCurrentLevelInside(true);
                }
                parent = parent.getParent();
            }

            for (Node node: newCurrentLevel.getChildren()) {
                if (node instanceof Collapsible) {
                    ((Collapsible)node).setIsCurrentLevelInside(false);
                }
            }
        }
    }

    private Container getCurrentMathLevel() {
        Container currentMathLevel;
        VisualComponent visualContainer = (VisualComponent)Hierarchy.getNearestAncestor(getCurrentLevel(), VisualComponent.class);
        if (visualContainer == null) {
            currentMathLevel = getMathModel().getRoot();
        } else {
            currentMathLevel = (Container)visualContainer.getReferencedComponent();
        }
        return currentMathLevel;
    }

    /**
     * @return Returns selection ordered the same way as the objects are ordered in the currently active group.
     */
    @Override
    public Collection<Node> getSelection() {
        return Collections.unmodifiableSet(selection);
    }

    @Override
    public boolean isGroupable(Node node) {
        return node instanceof VisualNode;
    }

    @Override
    public VisualGroup groupSelection() {
        VisualGroup group = null;
        Collection<Node> nodes = SelectionHelper.getGroupableCurrentLevelSelection(this);
        if (nodes.size() >= 1) {
            group = new VisualGroup();
            getCurrentLevel().add(group);
            getCurrentLevel().reparent(nodes, group);
            Point2D centre = TransformHelper.getSnappedCentre(nodes);
            VisualModelTransformer.translateNodes(nodes, -centre.getX(), -centre.getY());
            group.setPosition(centre);
            select(group);
        }
        return group;
    }

    @Override
    public VisualPage groupPageSelection() {
        VisualPage page = null;
        Collection<Node> nodes = SelectionHelper.getGroupableCurrentLevelSelection(this);
        if (nodes.size() >= 1) {
            PageNode pageNode = new PageNode();
            getCurrentMathLevel().add(pageNode);
            page = new VisualPage(pageNode);
            getCurrentLevel().add(page);
            reparent(page, this, getCurrentLevel(), nodes);
            Point2D pos = TransformHelper.getSnappedCentre(nodes);
            VisualModelTransformer.translateNodes(nodes, -pos.getX(), -pos.getY());
            page.setPosition(pos);
            select(page);
        }
        return page;
    }

    @Override
    public void ungroupSelection() {
        ArrayList<Node> toSelect = new ArrayList<Node>();
        for(Node node : SelectionHelper.getOrderedCurrentLevelSelection(this)) {
            if (node instanceof VisualGroup) {
                VisualGroup group = (VisualGroup)node;
                ArrayList<Node> nodesToReparent = new ArrayList<Node>(group.getChildren());
                toSelect.addAll(nodesToReparent);
                this.reparent(getCurrentLevel(), this, group, nodesToReparent);
                getCurrentLevel().remove(group);
            } else if (node instanceof VisualPage) {
                VisualPage page = (VisualPage)node;
                ArrayList<Node> nodesToReparent = new ArrayList<Node>(page.getChildren());
                toSelect.addAll(nodesToReparent);
                this.reparent(getCurrentLevel(), this, page, nodesToReparent);
                getMathModel().remove(page.getReferencedComponent());
                getCurrentLevel().remove(page);
            } else {
                toSelect.add(node);
            }
        }
        select(toSelect);
    }


    @Override
    public void deleteSelection() {
        // XXX: The order of removal influences the remaining selection because there are listeners that remove hanging connections and replica nodes.
        // Remove selected connections
        deleteSelection(new Func<Node, Boolean>() {
            @Override
            public Boolean eval(Node node) {
                return node instanceof VisualConnection;
            }
        });
        // Remove selected replica nodes
        deleteSelection(new Func<Node, Boolean>() {
            @Override
            public Boolean eval(Node node) {
                return node instanceof Replica;
            }
        });
        // Remove remaining selected nodes
        deleteSelection(new Func<Node, Boolean>() {
            @Override
            public Boolean eval(Node node) {
                return true;
            }
        });
    }

    private void deleteSelection(final Func<Node, Boolean> filter) {
        HashMap<Container, LinkedList<Node>> batches = new HashMap<Container, LinkedList<Node>>();
        for (Node node : selection) {
            LinkedList<Node> batch = null;
            if (node.getParent() instanceof Container) {
                Container container = (Container)node.getParent();
                if (filter.eval(node)) {
                    batch = batches.get(container);
                    if (batch == null) {
                        batch = new LinkedList<Node>();
                        batches.put(container, batch);
                    }
                    batch.add(node);
                }
            }
        }
        for (Container container : batches.keySet()) {
            container.remove(batches.get(container));
        }
    }

    private Point2D transformToCurrentSpace(Point2D pointInRootSpace) {
        Point2D newPoint = new Point2D.Double();
        TransformHelper.getTransform(getRoot(), currentLevel).transform(pointInRootSpace, newPoint);
        return newPoint;
    }

    @Override
    public Collection<Node> boxHitTest(Point2D p1, Point2D p2) {
        p1 = transformToCurrentSpace(p1);
        p2 = transformToCurrentSpace(p2);
        return HitMan.boxHitTest(currentLevel, p1, p2);
    }

    @Override
    public void addObserver(StateObserver obs) {
        observableState.addObserver(obs);
    }

    @Override
    public void removeObserver(StateObserver obs) {
        observableState.removeObserver(obs);
    }

    public void sendNotification(StateEvent e) {
        observableState.sendNotification(e);
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = new ModelProperties();
        if (node == null) {
            properties.add(new TitlePropertyDescriptor(this));
        }
        return properties;
    }

    public Collection<Node> getMathChildren(Collection<Node> nodes) {
        Collection<Node> ret = new HashSet<Node>();
        for (Node node: nodes) {
            if ((node instanceof Dependent) && !(node instanceof Replica)) {
                ret.addAll(((Dependent)node).getMathReferences());
            } else if (node instanceof VisualGroup) {
                ret.addAll(getMathChildren(node.getChildren()));
            }
        }
        return ret;
    }

    @Override
    public void reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<Node> srcChildren) {
        if (srcModel == null) {
            srcModel = this;
        }
        if (srcChildren == null) {
            srcChildren = srcRoot.getChildren();
        }

        Container srcMathContainer = NamespaceHelper.getMathContainer((VisualModel)srcModel, srcRoot);
        Collection<Node> srcMathChildren = getMathChildren(srcChildren);
        MathModel srcMathModel = ((VisualModel)srcModel).getMathModel();

        MathModel dstMathMmodel = getMathModel();
        Container dstMathContainer = NamespaceHelper.getMathContainer(this, dstContainer);
        dstMathMmodel.reparent(dstMathContainer, srcMathModel, srcMathContainer, srcMathChildren);

        // Save root-space position of components and set connections scale mode to follow components.
        HashMap<VisualTransformableNode, Point2D> componentToPositionMap = VisualModelTransformer.getRootSpacePositions(srcChildren);

        Collection<Node> dstChildren = new LinkedList<>(srcChildren);
        srcRoot.reparent(dstChildren, dstContainer);

        VisualModelTransformer.setRootSpacePositions(componentToPositionMap);
    }

    @Override
    public void setTemplateNode(VisualNode node) {
        templateNode = node;
        notifySelectionChanged(null);
    }

    @Override
    public VisualNode getTemplateNode() {
        return templateNode;
    }

    @Override
    @NoAutoSerialisation
    public String getTitle() {
        if (mathModel != null) {
            return mathModel.getTitle();
        } else {
            return super.getTitle();
        }
    }

    @Override
    @NoAutoSerialisation
    public void setTitle(String title) {
        if (mathModel != null) {
            mathModel.setTitle(title);
        } else {
            super.setTitle(title);
        }
        sendNotification(new ModelModifiedEvent(this));
    }

    @Override
    public AbstractLayoutTool getBestLayoutTool() {
        return new DotLayoutTool();
    }

}
