package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.MathModelListener;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.Model;
import org.workcraft.dom.XMLSerialisation;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionAnchorPoint;
import org.workcraft.framework.ComponentFactory;
import org.workcraft.framework.ConnectionFactory;
import org.workcraft.framework.GroupFactory;
import org.workcraft.framework.VisualNodeSerialiser;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.LoadFromXMLException;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.framework.exceptions.PasteException;
import org.workcraft.framework.exceptions.VisualComponentCreationException;
import org.workcraft.framework.exceptions.VisualConnectionCreationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.util.XmlUtil;

public class VisualModel implements Plugin, Model {

	class VisualPropertyChangeListener implements PropertyChangeListener {
		public void onPropertyChanged(String propertyName, Object sender) {
			if (sender instanceof VisualComponent)
				fireComponentPropertyChanged(propertyName, (VisualComponent)sender);
			else if (sender instanceof VisualConnection)
				fireConnectionPropertyChanged(propertyName, (VisualConnection)sender);
		}
	}

	public class RenamedVisualReferenceResolver implements VisualReferenceResolver {
		public VisualComponent getVisualComponentByID(int ID) {
			return VisualModel.this.getVisualComponentByRenamedID(ID);
		}
		public VisualConnection getVisualConnectionByID(int ID) {
			return VisualModel.this.getVisualConnectionByRenamedID(ID);
		}

		public Component getComponentByID(int ID) {
			return mathModel.getComponentByRenamedID(ID);
		}
		public Connection getConnectionByID(int ID) {
			return mathModel.getConnectionByRenamedID(ID);
		}
	}

	public class RegularVisualReferenceResolver implements VisualReferenceResolver {
		public VisualComponent getVisualComponentByID(int ID) {
			return VisualModel.this.getVisualComponentByID(ID);
		}
		public VisualConnection getVisualConnectionByID(int ID) {
			return VisualModel.this.getVisualConnectionByID(ID);
		}

		public Component getComponentByID(int ID) {
			return mathModel.getComponentByID(ID);
		}
		public Connection getConnectionByID(int ID) {
			return mathModel.getConnectionByID(ID);
		}
	}

	public VisualComponent getFirstVisualComponentByRefID(int ID) {
		for (VisualComponent vc: getVisualComponents()) {
			if (vc.isReferring(ID)) return vc;
		}
		return null;
	}


	public class ModelListener implements MathModelListener {
		public void onNodePropertyChanged(String propertyName, MathNode n) {
			if (n instanceof Component)
				fireComponentPropertyChanged(propertyName, getVisualComponentByID( ((Component)n).getID()));
		}

		public void onComponentAdded(Component component) {
		}

		public void onComponentRemoved(Component component) {
		}

		public void onConnectionAdded(Connection connection) {
		}

		public void onConnectionRemoved(Connection connection) {
		}
	}

	class Listener implements VisualModelEventListener {
//		private HashSet<VisualConnectionAnchorPoint> connectionAnchorPoints = new HashSet<VisualConnectionAnchorPoint>();

		public void onComponentAdded(VisualComponent component) {
		}

		public void onComponentPropertyChanged(String propertyName,
				VisualComponent component) {
		}

		public void onComponentRemoved(VisualComponent component) {
		}

		public void onConnectionAdded(VisualConnection connection) {
		}

		public void onConnectionPropertyChanged(String propertyName,
				VisualConnection connection) {
		}

		public void onConnectionRemoved(VisualConnection connection) {
		}

		public void onLayoutChanged() {
		}

		public void onSelectionChanged() {


//			for (VisualConnectionAnchorPoint p : connectionAnchorPoints)
//				p.getParentConnection().getParent().remove(p);
//
//			connectionAnchorPoints.clear();

//			for (VisualNode n : selection) {
//				if (n instanceof VisualConnection) {
//					VisualConnection con = (VisualConnection)n;
//
//					VisualConnectionAnchorPoint[] ap = con.getAnchorPointComponents();
//					for (VisualConnectionAnchorPoint p : ap)
//						con.getParent().add(p);
//				}
//			}
		}

		@Override
		public void onSelectionChanged(Collection<HierarchyNode> selection) {
			// TODO Auto-generated method stub

		}
	}

	private VisualPropertyChangeListener propertyChangeListener = new VisualPropertyChangeListener();
	private VisualReferenceResolver referenceResolver = new RenamedVisualReferenceResolver();

	private MathModel mathModel;
	private VisualGroup root;

	private LinkedList<HierarchyNode> selection = new LinkedList<HierarchyNode>();
	private LinkedList<VisualModelEventListener> listeners = new LinkedList<VisualModelEventListener>();

//	private HashMap<Integer, VisualComponent> refIDToVisualComponentMap = new HashMap<Integer, VisualComponent>();
//	protected HashMap<Integer, VisualConnection> refIDToVisualConnectionMap = new HashMap<Integer, VisualConnection>();
	private HashMap<Integer, VisualComponent> visualComponents = new HashMap<Integer, VisualComponent>();
	protected HashMap<Integer, VisualConnection> visualConnections = new HashMap<Integer, VisualConnection>();
	private HashMap<Integer, Integer> visualComponentRenames = new HashMap<Integer, Integer>();
	protected HashMap<Integer, Integer> visualConnectionRenames = new HashMap<Integer, Integer>();

	private XMLSerialisation serialiser = new XMLSerialisation();
	private ModelListener mathModelListener = new ModelListener();

	private void addXMLSerialisable() {
		serialiser.addSerialiser(new XMLSerialiser() {
			public String getTagName() {
				return VisualModel.class.getSimpleName();
			}
			public void serialise(Element element) {
				nodesToXML (element, root.getChildren());
			}
		});
	}

	protected final void createDefaultFlatStructure() throws VisualComponentCreationException, VisualConnectionCreationException {
		initRenames();

		for (Component component : mathModel.getComponents()) {
			VisualNode visualComponent;
			visualComponent = ComponentFactory.createVisualComponent(component);

			if (visualComponent != null) {
				getRoot().add(visualComponent);
				addComponents(visualComponent);

				// because visual nodes are just created, we use rename lists to map math nodes to visual ones
				visualComponentRenames.put(component.getID(), visualComponent.getID());
			}
		}

		for (Connection connection : mathModel.getConnections()) {
			VisualConnection visualConnection = ConnectionFactory.createVisualConnection(connection, getReferenceResolver());

			VisualNode.getCommonParent(visualConnection.getFirst(), visualConnection.getSecond()).add(visualConnection);
			addConnection(visualConnection);

			visualConnectionRenames.put(connection.getID(), visualConnection.getID());

		}
	}


	public VisualModel(MathModel model) throws VisualModelInstantiationException {
		mathModel = model;









		root = new VisualGroup();
		currentLevel = root;
		addXMLSerialisable();
		mathModel.addListener(mathModelListener);
		addListener(new Listener());
	}

	public VisualModel(MathModel mathModel, Element visualModelElement) throws VisualModelInstantiationException {
		this.mathModel = mathModel;
		root = new VisualGroup();
		currentLevel = root;

		try {
			Element element = XmlUtil.getChildElement(VisualModel.class.getSimpleName(), visualModelElement);
			loadFromXML(element);
		} catch (PasteException e) {
			throw new VisualModelInstantiationException(e);
		}

		addXMLSerialisable();

		mathModel.addListener(mathModelListener);
	}

	protected VisualNode createNode(Element element) throws VisualComponentCreationException, VisualConnectionCreationException
	{
		Integer oldID;
		Integer newID;
		if(element.getTagName() == "component") {

			VisualComponent vc = (VisualComponent)ComponentFactory.createVisualComponent(element, this);
			oldID = vc.getID();
			newID = addComponent(vc);
			visualComponentRenames.put(oldID, newID);
			return vc;
		}
		if(element.getTagName() == "connection") {
			VisualConnection vc = ConnectionFactory.createVisualConnection(element, getReferenceResolver());
			oldID = vc.getID();
			newID = addConnection(vc);
			visualConnectionRenames.put(oldID, newID);
			return vc;
		}
		if(element.getTagName() == "group") {
			return GroupFactory.createVisualGroup(element, this);
		}
		return null;
	}

	protected final void initRenames() {
		visualComponentRenames.clear();
		visualConnectionRenames.clear();
	}

	protected Collection<HierarchyNode> pasteFromXML (Element visualElement, Point2D location) throws PasteException {

		initRenames();

		List<Element> children = XmlUtil.getChildElements("component", visualElement);
		children.addAll(XmlUtil.getChildElements("group", visualElement));
		children.addAll(XmlUtil.getChildElements("connection", visualElement));

		LinkedList<HierarchyNode> pasted = new LinkedList<HierarchyNode>();

		try
		{
			for (Element e: children) {
				VisualNode node = createNode(e);
				if(node == null) continue;

				currentLevel.add(node);
				addComponents(node);

				pasted.add(node);
			}

			Rectangle2D nodesBB = VisualModel.getNodesBoundingBox(pasted);
			translateNodes(pasted, location.getX()-nodesBB.getCenterX(), location.getY()-nodesBB.getCenterY());

			return pasted;
		} catch (VisualConnectionCreationException e) {
			throw new PasteException (e);

		} catch (VisualComponentCreationException e) {
			throw new PasteException (e);
		}
	}

	// the same as pasteFromXML, with no offset applied
	protected Collection<VisualNode> loadFromXML (Element visualElement) throws PasteException {
		List<Element> children = XmlUtil.getChildElements("component", visualElement);
		children.addAll(XmlUtil.getChildElements("group", visualElement));
		children.addAll(XmlUtil.getChildElements("connection", visualElement));

		LinkedList<VisualNode> pasted = new LinkedList<VisualNode>();

		try
		{
			for (Element e: children) {
				VisualNode node;
				try
				{
					node = createNode(e);

				} catch (VisualConnectionCreationException ex) {
					//throw new PasteException (ex);
					node = null;
				}

				if(node == null) continue;

				currentLevel.add(node);
				addComponents(node);

				pasted.add(node);
			}

//			Rectangle2D nodesBB = VisualModel.getNodesBoundingBox(pasted);
//			translateNodes(pasted, location.getX()-nodesBB.getCenterX(), location.getY()-nodesBB.getCenterY());

			return pasted;

		} catch (VisualComponentCreationException e) {
			throw new PasteException (e);
		}
	}

	static void nodesToXML (Element parentElement, Collection <? extends HierarchyNode> nodes) {
		for (HierarchyNode node : nodes) {
			if (node instanceof VisualComponent) {
				VisualComponent vc = (VisualComponent)node;
				Element vcompElement = XmlUtil.createChildElement("component", parentElement);
				XmlUtil.writeIntAttr(vcompElement, "ref", vc.getReferencedComponent().getID());
				XmlUtil.writeStringAttr(vcompElement, "class", vc.getClass().getName());

				vc.serialiseToXML(vcompElement);
			} else if (node instanceof VisualConnection) {
				VisualConnection vc = (VisualConnection)node;
				Element vconElement = XmlUtil.createChildElement("connection", parentElement);
				XmlUtil.writeStringAttr(vconElement, "class", vc.getClass().getName());
				vc.serialiseToXML(vconElement);
			} else if (node instanceof VisualGroup) {
				Element childGroupElement = XmlUtil.createChildElement("group", parentElement);

				VisualNodeSerialiser serialiser = ((VisualGroup)node).getSerialiser();
				serialiser.serialise(node, childGroupElement);
				XmlUtil.writeStringAttr(childGroupElement, "class", node.getClass().getName());
			}
		}
	}

	private void gatherReferences(Collection<HierarchyNode> nodes, HashSet<MathNode> referenceds) {
		for (HierarchyNode n : nodes)
			if(n instanceof DependentNode)
				referenceds.addAll(((DependentNode)n).getMathReferences());
	}

	private static Rectangle2D bbUnion(Rectangle2D bb1, Rectangle2D bb2)
	{
		if(bb1 == null)
			return bb2;
		if(bb2 == null)
			return bb1;
		Rectangle2D.union(bb1, bb2, bb1);
		return bb1;
	}

	public static Rectangle2D getNodesBoundingBox(Collection<HierarchyNode> nodes) {
		Rectangle2D selectionBB = null;

		if (nodes.isEmpty()) return selectionBB;

		for (HierarchyNode vn: nodes) {
			if(vn instanceof Touchable)
				selectionBB = bbUnion(selectionBB, ((Touchable)vn).getBoundingBox());
		}
		return selectionBB;
	}

	public Rectangle2D getSelectionBoundingBox() {
		return getNodesBoundingBox(selection);
	}

	/*
	 * Apply transformation to each node position, if possible
	 * @author Stan
	 */
	public void transformNodePosition(Collection<HierarchyNode> nodes, AffineTransform t) {
		assert nodes!=null;
		Point2D np;
		for (HierarchyNode node: nodes) {
			if (node instanceof Movable) {
				// for all movable objects
				Movable mn = (Movable)node;
				np = new Point2D.Double(mn.getX(), mn.getY());
				t.transform(np, np);
				mn.setX(np.getX());
				mn.setY(np.getY());
			}
		}
	}

	public void translateNodes(Collection<HierarchyNode> nodes, double tx, double ty) {
		AffineTransform t = new AffineTransform();

		t.translate(tx, ty);

		transformNodePosition(nodes, t);
	}

	public void translateSelection(double tx, double ty) {
		translateNodes(selection, tx, ty);
	}

	public void scaleSelection(double sx, double sy) {
		Rectangle2D selectionBB = getSelectionBoundingBox();
		// create rotation matrix
		AffineTransform t = new AffineTransform();

		t.translate(selectionBB.getCenterX(), selectionBB.getCenterY());
		t.scale(sx, sy);
		t.translate(-selectionBB.getCenterX(), -selectionBB.getCenterY());

		// translate nodes by t
		transformNodePosition(selection, t);
	}

	public void rotateSelection(double theta) {
		Rectangle2D selectionBB = getSelectionBoundingBox();
		// create rotation matrix
		AffineTransform t = new AffineTransform();

		t.translate(selectionBB.getCenterX(), selectionBB.getCenterY());
		t.rotate(theta);
		t.translate(-selectionBB.getCenterX(), -selectionBB.getCenterY());

		// translate nodes by t
		transformNodePosition(selection, t);
	}

	private void selectionToXML(Element xmlElement) {
		Element mathElement = XmlUtil.createChildElement("model", xmlElement);
		XmlUtil.writeStringAttr(mathElement, "class", getMathModel().getClass().getName());
		Element visualElement = XmlUtil.createChildElement("visual-model", xmlElement);

		HashSet<MathNode> references = new HashSet<MathNode>();

		LinkedList <HierarchyNode> unselect = new LinkedList<HierarchyNode>();

		// remove partial connections from selection
		for (HierarchyNode vn : selection) {
			if (vn instanceof VisualConnection) {
				VisualConnection vc = (VisualConnection)vn;
				if (!selection.contains(vc.getFirst())||!selection.contains(vc.getSecond())) {
					unselect.add(vn);
				}
			}
		}

		for (HierarchyNode vn : unselect) {
			removeFromSelection(vn);
			if(vn instanceof Colorisable)
				((Colorisable)vn).clearColorisation();
		}

		if (!unselect.isEmpty()) {
			fireSelectionChanged();
		}


		gatherReferences (selection, references);

		nodesToXML(visualElement, selection);
		MathModel.nodesToXML(mathElement, references);
	}

	public void draw (Graphics2D g) {
		DrawMan.draw(g, root);
	}

	public VisualGroup getRoot() {
		return root;
	}

	/**
	 * Get the list of selected objects. Returned list is modifiable!
	 * @return the selection.
	 */
	public LinkedList<HierarchyNode> selection() {
		return selection;
	}

	/**
	 * Select all components, connections and groups from the <code>root</code> group.
	 */
	public void selectAll() {;
	selection.clear();
	selection.addAll(root.children);
	}

	/**
	 * Clear selection.
	 */
	public void selectNone() {
		selection.clear();
	}

	/**
	 * Check if the object is selected.<br/>
	 * <i>Important!</i> Slow function. It searches through all the selected objects,
	 * so it should not be called frequently.
	 * @param so selectable object
	 * @return if <code>so</code> is selected
	 */
	public boolean isObjectSelected(HierarchyNode so) {
		return selection.contains(so);
	}

	/**
	 * Add an object to the selection if it is not already selected.
	 * @param so an object to select
	 */
	public void addToSelection(HierarchyNode so) {
		if(!isObjectSelected(so))
			selection.add(so);
	}

	/**
	 * Remove an object from the selection if it is selected.
	 * @param so an object to deselect.
	 */
	public void removeFromSelection(HierarchyNode so) {
		selection.remove(so);
	}


	public MathModel getMathModel() {
		return mathModel;
	}


	public VisualModel getVisualModel() {
		return this;
	}


	public String getTitle() {
		return mathModel.getTitle();
	}


	public String getDisplayName() {
		return mathModel.getDisplayName();
	}

	public void addListener(VisualModelEventListener listener) {
		listeners.add(listener);
	}

	public void removeListener(VisualModelEventListener listener) {
		listeners.remove(listener);
	}

	public void fireComponentPropertyChanged(String propertyName, VisualComponent c) {
		for (VisualModelEventListener l : listeners)
			l.onComponentPropertyChanged(propertyName, c);
	}

	public void fireConnectionPropertyChanged(String propertyName, VisualConnection c) {
		for (VisualModelEventListener l : listeners)
			l.onConnectionPropertyChanged(propertyName, c);
	}

	public void fireLayoutChanged() {
		for (VisualModelEventListener l : listeners)
			l.onLayoutChanged();
	}

	public void fireSelectionChanged() {
		for (VisualModelEventListener l : listeners)
			l.onSelectionChanged(selection);
	}

	public void addListener(MathModelListener listener) {
		mathModel.addListener(listener);
	}

	public void removeListener (MathModelListener listener) {
		mathModel.removeListener(listener);
	}

	public VisualNode[] getSelection() {
		return selection.toArray(new VisualNode[0]);
	}

	public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
		if (first instanceof VisualComponent && second instanceof VisualComponent) {
			mathModel.validateConnection(new Connection (((VisualComponent)first).getReferencedComponent(),
					((VisualComponent)second).getReferencedComponent()));
		}
		else throw new InvalidConnectionException("Only connections between components are allowed");
	}

	public VisualConnection connect(VisualNode first, VisualNode second) throws InvalidConnectionException {
		validateConnection(first, second);

		VisualComponent firstComponent = (VisualComponent)first;
		VisualComponent secondComponent = (VisualComponent)second;

		Connection con = mathModel.connect(firstComponent.getReferencedComponent(), secondComponent.getReferencedComponent());
		VisualConnection ret = new VisualConnection(con, firstComponent, secondComponent);

		VisualGroup group = VisualNode.getCommonParent(first, second);

		group.add(ret);
		addConnection(ret);

		return ret;
	}

	public final void addComponents(HierarchyNode node) {
		if(node instanceof VisualComponent) {
			addComponent((VisualComponent)node);
		} else

		if(node instanceof VisualConnection) {
			addConnection((VisualConnection)node);
		} else

		if(node instanceof VisualGroup)
			for(HierarchyNode subnode : ((VisualGroup)node).getChildren())
				addComponents(subnode);

		else throw new RuntimeException("Unknown component type");
	}

	public final int addComponent(VisualComponent component) {
//		refIDToVisualComponentMap.put(component.getReferencedComponent().getID(), component);

		component.addPropertyChangeListener(propertyChangeListener);

		component.setID(getMathModel().getNextNodeID());

		visualComponents.put(component.getID(), component);

		fireComponentAdded(component);

		return component.getID();
	}

	private void fireComponentAdded(VisualComponent component) {
		for (VisualModelEventListener l : listeners)
			l.onComponentAdded(component);
	}

	public final int addConnection(VisualConnection connection) {
		connection.getFirst().addConnection(connection);
		connection.getSecond().addConnection(connection);

		connection.setID(getMathModel().getNextNodeID());

//		if (connection.getReferencedConnection() != null)
//			refIDToVisualConnectionMap.put(connection.getReferencedConnection().getID(), connection);
		visualConnections.put(connection.getID(), connection);

		connection.addPropertyChangeListener(propertyChangeListener);

		fireConnectionAdded(connection);

		return connection.getID();
	}

	private void fireConnectionAdded(VisualConnection connection) {
		for (VisualModelEventListener l : listeners)
			l.onConnectionAdded(connection);
	}

	public ArrayList<GraphEditorTool> getAdditionalTools() {
		return new ArrayList<GraphEditorTool>();
	}

	public void clearColorisation() {
		root.clearColorisation();
	}

	VisualGroup currentLevel;

	public VisualGroup getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(VisualGroup newCurrentLevel) {
		selection.clear();
		currentLevel = newCurrentLevel;
		fireSelectionChanged();
	}

	private List<VisualTransformableNode> getTransformableSelection()
	{
		ArrayList<VisualTransformableNode> result = new ArrayList<VisualTransformableNode>();
		for(HierarchyNode node : selection)
			if(node instanceof VisualTransformableNode)
				result.add((VisualTransformableNode)node);
		return result;
	}

	/**
	 * Groups the selection, and selects the newly created group.
	 * @author Arseniy Alekseyev
	 */
	public void groupSelection() {
		List<VisualTransformableNode> selected = getTransformableSelection();
		if(selected.size() <= 1)
			return;
		VisualGroup group = new VisualGroup();
		currentLevel.add(group);
		for(VisualNode node : selected)
		{
			if (node instanceof VisualConnectionAnchorPoint) {
				selection.remove(node);
				continue;
			}
			currentLevel.remove(node);
			group.add(node);
		}

		ArrayList<VisualConnection> connectionsToGroup = new ArrayList<VisualConnection>();
		for(VisualConnection connection : currentLevel.connections)
		{
			if(connection.getFirst().isDescendantOf(group) &&
					connection.getSecond().isDescendantOf(group)) {
				connectionsToGroup.add(connection);
			}
		}

		for(VisualConnection connection : connectionsToGroup)
			group.add(connection);

		selection.clear();
		selection.add(group);
		fireSelectionChanged();
	}

	/**
	 * Ungroups all groups in the current selection and adds the ungrouped components to the selection.
	 * @author Arseniy Alekseyev
	 */
	public void ungroupSelection() {
		ArrayList<VisualNode> unGrouped = new ArrayList<VisualNode>();

		for(VisualNode node : getSelection())
		{
			if(node instanceof VisualGroup)
			{
				VisualGroup group = (VisualGroup)node;
				for(VisualNode subNode : group.unGroup())
					unGrouped.add(subNode);
				currentLevel.remove(group);
			}
		}

		selection.clear();

		for(VisualNode node : unGrouped)
			selection.add(node);
		fireSelectionChanged();
	}

	protected void removeGroup(VisualGroup group) {
		removeNodes(group.getChildren());

		group.getParent().remove(group);
		selection.remove(group);
	}

	protected void removeComponent(VisualComponent component) {
		for (VisualConnection con : component.getConnections())
			removeConnection(con);
		mathModel.removeComponent(component.getReferencedComponent());

		selection.remove(component);
		component.getParent().remove(component);

		component.removePropertyChangeListener(propertyChangeListener);

		//refIDToVisualComponentMap.remove(component.getReferencedComponent().getID());
		visualComponents.remove(component.getID());

		fireComponentRemoved(component);
	}

	private void fireComponentRemoved(VisualComponent component) {
		for (VisualModelEventListener l : listeners)
			l.onComponentRemoved(component);
	}
	protected void removeConnection(VisualConnection connection) {
		connection.getFirst().removeConnection(connection);
		connection.getSecond().removeConnection(connection);
		mathModel.removeConnection(connection.getReferencedConnection());

		connection.getParent().remove(connection);
		selection.remove(connection);

		connection.removePropertyChangeListener(propertyChangeListener);
		//refIDToVisualConnectionMap.remove(connection.getReferencedConnection().getID());
		visualConnections.remove(connection.getID());

		fireConnectionRemoved(connection);
	}

//	protected void fireAnchorRemoved(VisualConnectionAnchorPoint anchor) {
//		for (VisualModelEventListener l : listeners)
//			l.onAnchorRemoved(anchor);
//	}

	protected void fireConnectionRemoved(VisualConnection connection) {
		for (VisualModelEventListener l : listeners)
			l.onConnectionRemoved(connection);
	}

	protected void removeNodes(Collection<HierarchyNode> nodes) {
		LinkedList<VisualConnection> connectionsToRemove = new LinkedList<VisualConnection>();
		LinkedList<VisualComponent> componentsToRemove = new LinkedList<VisualComponent>();
		LinkedList<VisualGroup> groupsToRemove = new LinkedList<VisualGroup>();
		LinkedList<VisualConnectionAnchorPoint> anchorsToRemove = new LinkedList<VisualConnectionAnchorPoint>();

		for (HierarchyNode n : nodes) {
			if (n instanceof VisualConnectionAnchorPoint)
				anchorsToRemove.add((VisualConnectionAnchorPoint)n);
			if (n instanceof VisualConnection)
				connectionsToRemove.add((VisualConnection)n);
			if (n instanceof VisualComponent)
				componentsToRemove.add((VisualComponent)n);
			if (n instanceof VisualGroup)
				groupsToRemove.add((VisualGroup)n);
		}

		for (VisualConnectionAnchorPoint an : anchorsToRemove) {
			connectionsToRemove.remove(an.getParentConnection());
		}
		for (VisualConnection con : connectionsToRemove)
			removeConnection(con);
		for (VisualComponent comp : componentsToRemove)
			removeComponent(comp);
		for (VisualGroup g: groupsToRemove)
			removeGroup(g);
	}

	/**
	 * Deletes the selection.
	 * @author Ivan Poliakov
	 */
	public void deleteSelection() {
		removeNodes(selection);
	}

	final public VisualComponent getVisualComponentByRenamedID(int oldID) {
		Integer newID = visualComponentRenames.get(oldID);
		if (newID == null)
			return null;
		return getVisualComponentByID(newID);
	}

	final public VisualConnection getVisualConnectionByRenamedID(int oldID) {
		Integer newID = visualConnectionRenames.get(oldID);
		if (newID == null)
			return null;
		return getVisualConnectionByID(newID);
	}


	/**
	 * @param clipboard
	 * @param clipboardOwner
	 * @author Ivan Poliakov
	 * @throws ParserConfigurationException
	 */
	public void copy(Clipboard clipboard, ClipboardOwner clipboardOwner) throws ParserConfigurationException {
		Document doc = XmlUtil.createDocument();

		Element root = doc.createElement("workcraft-clipboard-contents");

		doc.appendChild(root);
		root = doc.getDocumentElement();
		selectionToXML(root);
		clipboard.setContents(new TransferableDocument(doc), clipboardOwner);
	}

	public Collection<HierarchyNode> paste(Clipboard clipboard, Point2D where) throws PasteException {
		try {
			Document doc = (Document)clipboard.getData(TransferableDocument.DOCUMENT_FLAVOR);

			Element root = doc.getDocumentElement();
			if (!root.getTagName().equals("workcraft-clipboard-contents"))
				return null;

			Element mathModelElement = XmlUtil.getChildElement("model", root);
			Element visualModelElement = XmlUtil.getChildElement("visual-model", root);

			if (mathModelElement == null || visualModelElement == null)
				throw new PasteException("Structure of clipboard XML is invalid.");

			mathModel.pasteFromXML(mathModelElement);
			return pasteFromXML(visualModelElement, where);
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
			throw new PasteException (e);
		} catch (LoadFromXMLException e) {
			throw new PasteException (e);
		}

		return null;
	}

	public void select(Collection<VisualNode> nodes) {
		selectNone();
		for (VisualNode n: nodes)
			addToSelection(n);
	}

	public void cut(Clipboard clipboard, ClipboardOwner clipboardOwner) throws ParserConfigurationException {
		copy(clipboard, clipboardOwner);
		deleteSelection();
	}

//	public VisualComponent getComponentByRefID(Integer id) {
//		return refIDToVisualComponentMap.get(id);
//	}

	public VisualComponent getVisualComponentByID(Integer id) {
		return visualComponents.get(id);
	}

	public VisualConnection getVisualConnectionByID(Integer id) {
		return visualConnections.get(id);
	}

	public HashSet<VisualComponent> getVisualComponents() {
		return new HashSet<VisualComponent>(visualComponents.values());
	}


	private Point2D transformToCurrentSpace(Point2D pointInRootSpace)
	{
		if(currentLevel == root)
			return pointInRootSpace;
		Point2D newPoint = new Point2D.Double();
		try {
			currentLevel.getAncestorToParentTransform(root).transform(pointInRootSpace, newPoint);
		} catch (NotAnAncestorException e) {
			e.printStackTrace();
			throw new RuntimeException("Root is not an ancestor of the current node o_O");
		}
		return newPoint;
	}

	public HierarchyNode hitNode(Point2D pointInRootSpace)
	{
		return HitMan.hitTestForSelection(transformToCurrentSpace(pointInRootSpace), currentLevel);
	}

	public LinkedList<Touchable> hitObjects(Point2D p1, Point2D p2) {
		p1 = transformToCurrentSpace(p1);
		p2 = transformToCurrentSpace(p2);
		return currentLevel.hitObjects(p1, p2);
	}

	public LinkedList<Touchable> hitObjects(Rectangle2D selectionRect) {
		Point2D min = new Point2D.Double(selectionRect.getMinX(), selectionRect.getMinY());
		Point2D max = new Point2D.Double(selectionRect.getMaxX(), selectionRect.getMaxY());
		min = transformToCurrentSpace(min);
		max = transformToCurrentSpace(max);
		selectionRect.setRect(min.getX(), min.getY(), max.getX()-min.getX(), max.getY()-min.getY());

		return currentLevel.hitObjects(selectionRect);
	}

	public final void addXMLSerialisable(XMLSerialiser serialisable) {
		serialiser.addSerialiser(serialisable);
	}

	public final void serialiseToXML(Element componentElement) {
		serialiser.serialise(componentElement);
	}

	protected VisualPropertyChangeListener getPropertyChangeListener() {
		return propertyChangeListener;
	}

	protected VisualReferenceResolver getReferenceResolver() {
		return referenceResolver;
	}
}
