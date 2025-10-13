package org.workcraft.dom.visual;

import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.*;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.FlatReferenceManager;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.observation.*;
import org.workcraft.plugins.builtin.commands.DotLayoutCommand;
import org.workcraft.plugins.builtin.commands.RandomLayoutCommand;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.types.Func;
import org.workcraft.types.Pair;
import org.workcraft.types.Triple;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.SetUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Queue;
import java.util.*;

public abstract class AbstractVisualModel extends AbstractModel<VisualNode, VisualConnection> implements VisualModel {

    public static final String PROPERTY_TITLE = "Title";

    private final MathModel mathModel;
    private Container currentLevel;
    private final Set<VisualNode> selection = new HashSet<>();
    private final ObservableStateImpl observableState = new ObservableStateImpl();
    private final List<GraphEditorTool> graphEditorTools = new ArrayList<>();

    public AbstractVisualModel() {
        this(null, null);
    }

    public AbstractVisualModel(MathModel mathModel) {
        this(mathModel, null);
    }

    public AbstractVisualModel(MathModel mathModel, VisualGroup root) {
        super(root);
        this.mathModel = mathModel;
        this.currentLevel = getRoot();

        registerGraphEditorTools();

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
        if (generatedRoot) {
            createDefaultStructure();
        }
    }

    @Override
    public void applyRandomLayout(Point2D start, Point2D range) {
        Random r = new Random();
        Queue<Triple<Container, Point2D, Point2D>> queue = new ArrayDeque<>();
        queue.add(Triple.of(getRoot(), start, range));
        while (!queue.isEmpty()) {
            Triple<Container, Point2D, Point2D> item = queue.remove();
            Container container = item.getFirst();
            start = item.getSecond();
            range = item.getThird();
            for (Node node : container.getChildren()) {
                double x = start.getX() + r.nextDouble() * range.getX();
                double y = start.getY() + r.nextDouble() * range.getY();
                if (node instanceof VisualTransformableNode transformableNode) {
                    transformableNode.setRootSpacePosition(new Point2D.Double(x, y));
                }
                if (node instanceof Container) {
                    Point2D childrenRange = new Point2D.Double(range.getX() / 2.0, range.getY() / 2.0);
                    Point2D childrenStart = new Point2D.Double(x - childrenRange.getX() / 2.0, y - childrenRange.getY() / 2.0);
                    queue.add(Triple.of((Container) node, childrenStart, childrenRange));
                }
            }
        }
        for (VisualConnection connection : Hierarchy.getDescendantsOfType(getRoot(), VisualConnection.class)) {
            connection.setConnectionType(VisualConnection.ConnectionType.POLYLINE);
            connection.getGraphic().setDefaultControlPoints();
        }
    }

    @Override
    public VisualGroup createDefaultRoot() {
        return new VisualGroup();
    }

    @Override
    public FlatReferenceManager createDefaultReferenceManager() {
        return new FlatReferenceManager();
    }

    @Override
    public void createDefaultStructure() {
        HashMap<MathNode, VisualComponent> createdNodes = new HashMap<>();
        // Create components
        Queue<Pair<Container, Container>> containerQueue = new ArrayDeque<>();
        containerQueue.add(new Pair<>(getMathModel().getRoot(), getRoot()));
        while (!containerQueue.isEmpty()) {
            Pair<Container, Container> container = containerQueue.remove();
            Container mathContainer = container.getFirst();
            Container visualContainer = container.getSecond();
            for (Node node : mathContainer.getChildren()) {
                if (node instanceof MathConnection) continue;
                if (node instanceof MathNode mathNode) {
                    VisualComponent visualComponent = null;
                    try {
                        visualComponent = NodeFactory.createVisualComponent(mathNode);
                    } catch (NodeCreationException e) {
                        throw new RuntimeException(e);
                    }
                    if (visualComponent != null) {
                        visualContainer.add(visualComponent);
                        createdNodes.put(mathNode, visualComponent);
                    }
                    if ((mathNode instanceof Container) && (visualComponent instanceof Container)) {
                        containerQueue.add(new Pair<>((Container) mathNode, (Container) visualComponent));
                    }
                }
            }
        }
        // Create connections
        containerQueue.add(new Pair<>(getMathModel().getRoot(), getRoot()));
        while (!containerQueue.isEmpty()) {
            Pair<Container, Container> container = containerQueue.remove();
            Container mathContainer = container.getFirst();
            for (Node node : mathContainer.getChildren()) {
                if (node instanceof MathConnection mathConnection) {
                    VisualComponent firstComponent = createdNodes.get(mathConnection.getFirst());
                    VisualComponent secondComponent = createdNodes.get(mathConnection.getSecond());
                    try {
                        connect(firstComponent, secondComponent, mathConnection);
                    } catch (InvalidConnectionException e) {
                        throw new RuntimeException(e);
                    }
                } else if (node instanceof MathNode mathNode) {
                    VisualComponent visualComponent = createdNodes.get(mathNode);
                    if ((mathNode instanceof Container) && (visualComponent instanceof Container)) {
                        containerQueue.add(new Pair<>((Container) mathNode, (Container) visualComponent));
                    }
                }
            }
        }
    }

    @Override
    public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
        if (getConnection(first, second) != null) {
            throw new InvalidConnectionException("Connection already exists.");
        }

        if (!(first instanceof VisualComponent) || !(second instanceof VisualComponent)) {
            throw new InvalidConnectionException("Invalid connection.");
        }

        getMathModel().validateConnection(
                ((VisualComponent) first).getReferencedComponent(),
                ((VisualComponent) second).getReferencedComponent());
    }

    @Override
    public VisualConnection connect(VisualNode first, VisualNode second, MathConnection mConnection)
            throws InvalidConnectionException {

        validateConnection(first, second);
        if (mConnection == null) {
            mConnection = getMathModel().connect(getReferencedComponent(first), getReferencedComponent(second));
        }
        VisualConnection vConnection = new VisualConnection(mConnection, first, second);
        Container container = Hierarchy.getNearestContainer(first, second);
        container.add(vConnection);
        return vConnection;
    }

    @Override
    public VisualConnection connect(VisualNode first, VisualNode second) throws InvalidConnectionException {
        return connect(first, second, null);
    }

    @Override
    public void validateUndirectedConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
        validateConnection(first, second);
    }

    @Override
    public VisualConnection connectUndirected(VisualNode first, VisualNode second) throws InvalidConnectionException {
        validateUndirectedConnection(first, second);
        return connect(first, second, null);
    }

    public MathNode getReferencedComponent(VisualNode node) {
        VisualComponent component = null;
        if (node instanceof VisualComponent) {
            component = (VisualComponent) node;
        } else if (node instanceof VisualReplica) {
            component = ((VisualReplica) node).getMaster();
        }
        return (component == null) ? null : component.getReferencedComponent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends VisualComponent> T createVisualComponent(MathNode mathNode, Class<T> type) {
        VisualComponent component = null;
        Node mathParent = mathNode.getParent();
        if (mathParent == null) {
            mathModel.getRoot().add(mathNode);
        }
        Container container = getRoot();
        if (mathParent instanceof PageNode mathPage) {
            container = getVisualComponent(mathPage, VisualPage.class);
        }
        try {
            component = NodeFactory.createVisualComponent(mathNode);
            container.add(component);
        } catch (NodeCreationException e) {
            String mathName = getMathName(mathNode);
            throw new RuntimeException("Cannot create visual component for math node '" + mathName + "' of class '" + type + "'");
        }
        return (T) component;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends VisualComponent> T createVisualComponent(MathNode mathNode, Class<T> type, Container container) {
        VisualComponent component = null;
        if (container == null) {
            container = getRoot();
        }
        try {
            component = NodeFactory.createVisualComponent(mathNode);
            container.add(component);
        } catch (NodeCreationException e) {
            String mathName = getMathName(mathNode);
            throw new RuntimeException("Cannot create visual component for math node '" + mathName + "' of class '" + type + "'");
        }
        return (T) component;
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

    @Override
    public <T extends VisualComponent> T getVisualComponentByMathReference(String ref, Class<T> type) {
        T result = null;
        MathNode node = getMathModel().getNodeByReference(ref);
        if (node != null) {
            result = getVisualComponent(node, type);
        }
        return result;
    }

    @Override
    public VisualComponent getVisualComponentByMathReference(String ref) {
        return getVisualComponentByMathReference(ref, VisualComponent.class);
    }

    @Override
    public <T extends VisualReplica> T createVisualReplica(VisualComponent masterComponent, Class<T> type, Container container) {
        T replica = null;
        try {
            replica = NodeFactory.createNode(type);
        } catch (NodeCreationException e) {
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
    public VisualPage createVisualPage(Container container) {
        if (container == null) {
            container = getRoot();
        }
        VisualPage page = new VisualPage(new PageNode());
        container.add(page);

        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        mathContainer.add(page.getReferencedComponent());
        return page;
    }

    @Override
    public void draw(Graphics2D g, Decorator decorator) {
        DrawMan.draw(this, g, decorator, getRoot());
    }

    private void notifySelectionChanged(Collection<? extends VisualNode> oldSelection) {
        sendNotification(new SelectionChangedEvent(this, oldSelection));
    }

    /**
     * Select all components, connections and groups from the <code>root</code> group.
     */
    @Override
    public void selectAll() {
        if (selection.size() == getCurrentLevel().getChildren().size()) {
            return;
        }
        Collection<VisualNode> s = new HashSet<>(selection);
        selection.clear();
        Collection<VisualNode> nodes = NodeHelper.filterByType(getCurrentLevel().getChildren(), VisualNode.class);
        selection.addAll(nodes);
        notifySelectionChanged(s);
    }

    /**
     * Clear selection.
     */
    @Override
    public void selectNone() {
        if (!selection.isEmpty()) {
            Collection<VisualNode> s = new HashSet<>(selection);
            selection.clear();
            notifySelectionChanged(s);
        }
    }

    /**
     * Invert selection.
     */
    @Override
    public void selectInverse() {
        Collection<VisualNode> s = new HashSet<>(selection);
        selection.clear();
        Collection<VisualNode> nodes = NodeHelper.filterByType(getCurrentLevel().getChildren(), VisualNode.class);
        for (VisualNode node: nodes) {
            if (!s.contains(node)) {
                selection.add(node);
            }
        }
        notifySelectionChanged(nodes);
    }

    private void validateSelection(VisualNode node) {
        if (!Hierarchy.isDescendant(node, getCurrentLevel())) {
            throw new RuntimeException(
                "Cannot select a node that is not in the current editing level ("
                + node + "), parent (" + node.getParent() + ")");
        }
    }

    private void validateSelection(Collection<? extends VisualNode> nodes) {
        for (VisualNode node : nodes) {
            validateSelection(node);
        }
    }

    public boolean isSelected(Node node) {
        return selection.contains(node);
    }

    @Override
    public void select(VisualNode node) {
        if ((selection.size() == 1) && selection.contains(node)) {
            return;
        }
        validateSelection(node);
        Collection<VisualNode> oldSelection = new HashSet<>(selection);
        selection.clear();
        selection.add(node);
        notifySelectionChanged(oldSelection);
    }

    @Override
    public void select(Collection<? extends VisualNode> nodes) {
        Collection<VisualNode> s = new HashSet<>(selection);
        validateSelection(nodes);
        selection.clear();
        selection.addAll(nodes);
        notifySelectionChanged(s);
    }

    @Override
    public void toggleSelection(VisualNode node) {
        if (selection.contains(node)) {
            removeFromSelection(node);
        } else {
            addToSelection(node);
        }
    }

    @Override
    public void toggleSelection(Collection<? extends VisualNode> nodes) {
        select(SetUtils.symmetricDifference(new HashSet<>(selection), new HashSet<>(nodes)));
    }

    @Override
    public void addToSelection(VisualNode node) {
        if (!selection.contains(node)) {
            validateSelection(node);
            Collection<VisualNode> oldSelection = new HashSet<>(selection);
            selection.add(node);
            notifySelectionChanged(oldSelection);
        }
    }

    @Override
    public void addToSelection(Collection<? extends VisualNode> nodes) {
        validateSelection(nodes);
        Collection<VisualNode> oldSelection = new HashSet<>(selection);
        selection.addAll(nodes);
        if (oldSelection.size() != selection.size()) {
            notifySelectionChanged(oldSelection);
        }
    }

    @Override
    public void removeFromSelection(VisualNode node) {
        if (selection.contains(node)) {
            Collection<VisualNode> oldSelection = new HashSet<>(selection);
            selection.remove(node);
            notifySelectionChanged(oldSelection);
        }
    }

    @Override
    public void removeFromSelection(Collection<? extends VisualNode> nodes) {
        Collection<VisualNode> oldSelection = new HashSet<>(selection);
        selection.removeAll(nodes);
        if (oldSelection.size() != selection.size()) {
            notifySelectionChanged(oldSelection);
        }
    }

    @Override
    public MathModel getMathModel() {
        return mathModel;
    }

    @Override
    public String getMathReference(Node node) {
        if (node instanceof VisualComponent component) {
            node = component.getReferencedComponent();
        } else if (node instanceof VisualConnection connection) {
            node = connection.getReferencedConnection();
        }
        return node == null ? null : getMathModel().getNodeReference(node);
    }

    @Override
    public String getMathName(Node node) {
        if (node instanceof VisualComponent component) {
            node = component.getReferencedComponent();
        } else if (node instanceof VisualConnection connection) {
            node = connection.getReferencedConnection();
        }
        return node == null ? null : getMathModel().getName(node);
    }

    @Override
    public void setMathName(Node node, String name) {
        if (node instanceof VisualComponent component) {
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
            collapsible = (Collapsible) newCurrentLevel;
        }

        if (collapsible != null) {
            collapsible.setIsCurrentLevelInside(true);
            Node parent = newCurrentLevel.getParent();
            while (parent != null) {
                if (parent instanceof Collapsible) {
                    ((Collapsible) parent).setIsCurrentLevelInside(true);
                }
                parent = parent.getParent();
            }

            for (Node node: newCurrentLevel.getChildren()) {
                if (node instanceof Collapsible) {
                    ((Collapsible) node).setIsCurrentLevelInside(false);
                }
            }
        }
    }

    /**
     * @return Returns selection ordered the same way as the objects are ordered in the currently active group.
     */
    @Override
    public Collection<VisualNode> getSelection() {
        return Collections.unmodifiableSet(selection);
    }

    @Override
    public boolean isGroupable(VisualNode node) {
        return true;
    }

    @Override
    public VisualGroup groupSelection() {
        VisualGroup group = null;
        Collection<VisualNode> nodes = SelectionHelper.getGroupableCurrentLevelSelection(this);
        if (!nodes.isEmpty()) {
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
        Collection<VisualNode> nodes = SelectionHelper.getGroupableCurrentLevelSelection(this);
        if (!nodes.isEmpty()) {
            Container container = getCurrentLevel();
            page = createVisualPage(container);
            if (reparent(page, this, container, nodes)) {
                Point2D pos = TransformHelper.getSnappedCentre(nodes);
                VisualModelTransformer.translateNodes(nodes, -pos.getX(), -pos.getY());
                page.setPosition(pos);
                select(page);
            }
        }
        return page;
    }

    @Override
    public void ungroupSelection() {
        ArrayList<VisualNode> toSelect = new ArrayList<>();
        for (VisualNode node : SelectionHelper.getOrderedCurrentLevelSelection(this)) {
            if (node instanceof VisualGroup group) {
                Collection<VisualNode> nodesToReparent = NodeHelper.filterByType(group.getChildren(), VisualNode.class);
                toSelect.addAll(nodesToReparent);
                if (reparent(getCurrentLevel(), this, group, nodesToReparent)) {
                    getCurrentLevel().remove(group);
                }
            } else if (node instanceof VisualPage page) {
                Collection<VisualNode> nodesToReparent = NodeHelper.filterByType(page.getChildren(), VisualNode.class);
                toSelect.addAll(nodesToReparent);
                if (reparent(getCurrentLevel(), this, page, nodesToReparent)) {
                    getMathModel().remove(page.getReferencedComponent());
                    getCurrentLevel().remove(page);
                }
            } else {
                toSelect.add(node);
            }
        }
        select(toSelect);
    }

    @Override
    public void deleteSelection() {
        // Note: The order of removal influences the remaining selection because
        // there are listeners that remove hanging connections and replica nodes.
        // Remove selected connections
        deleteSelection(node -> node instanceof VisualConnection);
        // Remove selected replica nodes
        deleteSelection(node -> node instanceof Replica);
        // Remove remaining selected nodes
        deleteSelection(node -> true);
    }

    private void deleteSelection(final Func<Node, Boolean> filter) {
        HashMap<Container, LinkedList<Node>> batches = new HashMap<>();
        for (Node node : selection) {
            if (node.getParent() instanceof Container container) {
                if (filter.eval(node)) {
                    List<Node> batch = batches.computeIfAbsent(container, k -> new LinkedList<>());
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
    public Collection<VisualNode> hitBox(Point2D p1, Point2D p2) {
        p1 = transformToCurrentSpace(p1);
        p2 = transformToCurrentSpace(p2);
        return HitMan.hitBox(currentLevel, p1, p2);
    }

    @Override
    public Point2D getNodeSpacePosition(Point2D rootspacePosition, VisualTransformableNode node) {
        AffineTransform rootToParentTransform = TransformHelper.getTransform(getRoot(), node);
        Point2D localPosition = rootToParentTransform.transform(rootspacePosition, null);

        AffineTransform parentToNodeTransform = node.getParentToLocalTransform();
        return parentToNodeTransform.transform(localPosition, null);
    }

    @Override
    public void addObserver(StateObserver obs) {
        observableState.addObserver(obs);
    }

    @Override
    public void removeObserver(StateObserver obs) {
        observableState.removeObserver(obs);
    }

    @Override
    public void sendNotification(StateEvent e) {
        observableState.sendNotification(e);
    }

    public Collection<MathNode> getMathChildren(Collection<? extends VisualNode> nodes) {
        Collection<MathNode> ret = new HashSet<>();
        for (Node node: nodes) {
            if ((node instanceof Dependent) && !(node instanceof Replica)) {
                ret.addAll(((Dependent) node).getMathReferences());
            } else if (node instanceof VisualGroup) {
                Collection<VisualNode> children = NodeHelper.filterByType(node.getChildren(), VisualNode.class);
                ret.addAll(getMathChildren(children));
            }
        }
        return ret;
    }

    @Override
    public boolean reparent(Container dstContainer, Model<?, ?> srcModel, Container srcRoot, Collection<? extends VisualNode> srcChildren) {
        if (srcModel == null) {
            srcModel = this;
        }
        if (srcChildren == null) {
            srcChildren = NodeHelper.filterByType(srcRoot.getChildren(), VisualNode.class);
        }

        Container srcMathContainer = NamespaceHelper.getMathContainer((VisualModel) srcModel, srcRoot);
        Collection<MathNode> srcMathChildren = getMathChildren(srcChildren);
        MathModel srcMathModel = ((VisualModel) srcModel).getMathModel();

        MathModel dstMathMmodel = getMathModel();
        Container dstMathContainer = NamespaceHelper.getMathContainer(this, dstContainer);
        if (!dstMathMmodel.reparent(dstMathContainer, srcMathModel, srcMathContainer, srcMathChildren)) {
            return false;
        }
        // Save root-space position of components and set connections scale mode to follow components.
        HashMap<VisualTransformableNode, Point2D> componentToPositionMap = VisualModelTransformer.getRootSpacePositions(srcChildren);
        Collection<Node> dstChildren = new LinkedList<>(srcChildren);
        srcRoot.reparent(dstChildren, dstContainer);
        VisualModelTransformer.setRootSpacePositions(componentToPositionMap);
        return true;
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
    public AbstractLayoutCommand getBestLayouter() {
        return new DotLayoutCommand();
    }

    @Override
    public AbstractLayoutCommand getFallbackLayouter() {
        return new RandomLayoutCommand();
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return BoundingBoxHelper.mergeBoundingBoxes(Hierarchy.getChildrenOfType(getRoot(), Touchable.class));
    }

    @Override
    public void registerGraphEditorTools() {
    }

    @Override
    public final void addGraphEditorTool(GraphEditorTool tool) {
        graphEditorTools.add(tool);
    }

    @Override
    public final void removeGraphEditorTool(GraphEditorTool tool) {
        graphEditorTools.remove(tool);
    }

    @Override
    public final List<GraphEditorTool> getGraphEditorTools() {
        return Collections.unmodifiableList(graphEditorTools);
    }

    @Override
    public ModelProperties getProperties(VisualNode node) {
        ModelProperties properties = new ModelProperties();
        if (node == null) {
            properties.add(getTitleProperty());
        } else {
            String name = getMathName(node);
            if ((name != null) && !Identifier.hasInternalPrefix(name)) {
                properties.add(getNameProperty(node));
            }
        }
        return properties;
    }

    private PropertyDescriptor<?> getTitleProperty() {
        return new PropertyDeclaration<>(String.class, PROPERTY_TITLE, this::setTitle, this::getTitle);
    }

    private PropertyDescriptor<?> getNameProperty(VisualNode node) {
        String name = getMathName(node);
        return new PropertyDeclaration<>(String.class, Model.PROPERTY_NAME,
                value -> {
                    Identifier.validate(value);
                    if (node instanceof VisualComponent component) {
                        if (component.getReferencedComponent() instanceof NamespaceProvider) {
                            value = Identifier.appendNamespaceSeparator(value);
                        }
                    }
                    if (!value.equals(name)) {
                        setMathName(node, value);
                        node.sendNotification(new PropertyChangedEvent(node, Model.PROPERTY_NAME));
                    }
                },
                () -> name == null ? null : Identifier.truncateNamespaceSeparator(name));
    }

}
