package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.MathModelListener;
import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.util.XmlUtil;

public class VisualModel implements Plugin, Model {
	protected MathModel mathModel;
	protected VisualGroup root;

	protected LinkedList<VisualNode> selection = new LinkedList<VisualNode>();
	protected LinkedList<VisualModelListener> listeners = new LinkedList<VisualModelListener>();

	protected HashMap<Integer, VisualComponent> refIDToVisualComponentMap = new HashMap<Integer, VisualComponent>();
	protected HashMap<Integer, VisualConnection> refIDToVisualConnectionMap = new HashMap<Integer, VisualConnection>();

	public VisualModel(MathModel model) throws VisualModelConstructionException {
		mathModel = model;
		root = new VisualGroup();
		currentLevel = root;


		// create a default flat structure
		for (Component component : model.getComponents()) {
			VisualComponent visualComponent = PluginManager.createVisualComponent(component);
			if (visualComponent != null) {
				root.add(visualComponent);
				addComponent(visualComponent);
			}
		}

		for (Connection connection : model.getConnections()) {

			VisualConnection visualConnection = PluginManager.createVisualConnection(connection, getComponentByRefID(connection.getFirst().getID()),
																						getComponentByRefID(connection.getSecond().getID()));
			if (visualConnection != null) {
				root.add(visualConnection);
				addConnection(visualConnection);
			}
		}
	}

	public VisualModel(MathModel mathModel, Element visualElement) throws VisualModelConstructionException {
		this.mathModel = mathModel;

		// load structure from XML
		List<Element> nodes = XmlUtil.getChildElements("group", visualElement);

		if (nodes.size() != 1)
			throw new VisualModelConstructionException ("<visual-model> section of the document must contain one, and only one root group");

		root = new VisualGroup (nodes.get(0), this);
		currentLevel = root;
	}

	public static void nodesToXml (Element parentElement, Collection <? extends VisualNode> nodes) {
		for (VisualNode node : nodes) {
			if (node instanceof VisualComponent) {
				VisualComponent vc = (VisualComponent)node;
				Element vcompElement = XmlUtil.createChildElement("component", parentElement);
				XmlUtil.writeIntAttr(vcompElement, "ref", vc.getReferencedComponent().getID());
				vc.toXML(vcompElement);
			} else if (node instanceof VisualConnection) {
				VisualConnection vc = (VisualConnection)node;
				Element vconElement = XmlUtil.createChildElement("connection", parentElement);
				XmlUtil.writeIntAttr(vconElement, "ref", vc.getReferencedConnection().getID());
				vc.toXML(vconElement);
			} else if (node instanceof VisualGroup) {
				Element childGroupElement = XmlUtil.createChildElement("group", parentElement);
				((VisualGroup)node).toXML(childGroupElement);
			}
		}
	}

	public void toXML(Element xmlVisualElement) {
		// create root group element
		Element rootGroupElement = xmlVisualElement.getOwnerDocument().createElement("group");
		root.toXML(rootGroupElement);
		xmlVisualElement.appendChild(rootGroupElement);
	}

	public void selectionToXML(Element xmlElement) {
		Element mathElement = XmlUtil.createChildElement("model", xmlElement);
		XmlUtil.writeStringAttr(mathElement, "class", getMathModel().getClass().getName());
		Element visualElement = XmlUtil.createChildElement("visual-model", xmlElement);
		XmlUtil.writeStringAttr(visualElement, "class", getClass().getName());

		LinkedList<Component> referencedComponents = new LinkedList<Component>();
		LinkedList<Connection> referencedConnections = new LinkedList<Connection>();

		for (VisualNode n : selection)
			if (n instanceof VisualComponent)
				referencedComponents.add( ((VisualComponent)n).getReferencedComponent());
			else if (n instanceof VisualConnection)
				referencedConnections.add( ((VisualConnection)n).getReferencedConnection());

		VisualModel.nodesToXml(visualElement, selection);
		MathModel.componentsToXML(mathElement, referencedComponents);
		MathModel.connectionsToXML(mathElement, referencedConnections);
	}

	public void draw (Graphics2D g) {
		root.draw(g);
	}

	public VisualGroup getRoot() {
		return root;
	}

	/**
	 * Get the list of selected objects. Returned list is modifiable!
	 * @return the selection.
	 */
	public LinkedList<VisualNode> selection() {
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
	public boolean isObjectSelected(VisualNode so) {
		return selection.contains(so);
	}

	/**
	 * Add an object to the selection if it is not already selected.
	 * @param so an object to select
	 */
	public void addToSelection(VisualNode so) {
		if(!isObjectSelected(so))
			selection.add(so);
	}

	/**
	 * Remove an object from the selection if it is selected.
	 * @param so an object to deselect.
	 */
	public void removeFromSelection(VisualNode so) {
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

	public void addListener(VisualModelListener listener) {
		listeners.add(listener);
	}

	public void removeListener(VisualModelListener listener) {
		listeners.remove(listener);
	}

	public void fireModelStructureChanged() {
		for (VisualModelListener l : listeners)
			l.modelStructureChanged();
		mathModel.fireModelStructureChanged();
	}

	public void fireComponentPropertyChanged(Component c) {
		for (VisualModelListener l : listeners)
			l.componentPropertyChanged(c);

		mathModel.fireComponentPropertyChanged(c);
	}

	public void fireConnectionPropertyChanged(Connection c) {
		for (VisualModelListener l : listeners)
			l.connectionPropertyChanged(c);

		mathModel.fireConnectionPropertyChanged(c);
	}

	public void fireVisualNodePropertyChanged(VisualNode n) {
		for (VisualModelListener l : listeners)
			l.visualNodePropertyChanged(n);
	}

	public void fireLayoutChanged() {
		for (VisualModelListener l : listeners)
			l.layoutChanged();
	}

	public void fireSelectionChanged() {
		for (VisualModelListener l : listeners)
			l.selectionChanged();
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

	public void validateConnection(VisualComponent first, VisualComponent second) throws InvalidConnectionException {
		mathModel.validateConnection(new Connection (first.getReferencedComponent(), second.getReferencedComponent()));
	}

	public VisualConnection connect(VisualComponent first, VisualComponent second) throws InvalidConnectionException {
		Connection con = mathModel.connect(first.getReferencedComponent(), second.getReferencedComponent());
		VisualConnection ret = new VisualConnection(con, first, second);
		root.add(ret);
		addConnection(ret);
		connectionAdded(ret);

		fireModelStructureChanged();
		return ret;
	}

	protected final void addComponent(VisualComponent component) {
		refIDToVisualComponentMap.put(component.getReferencedComponent().getID(), component);
		componentAdded(component);
	}

	protected final void addConnection(VisualConnection connection) {
		connection.getFirst().addConnection(connection);
		connection.getSecond().addConnection(connection);

		refIDToVisualConnectionMap.put(connection.getReferencedConnection().getID(), connection);
		connectionAdded(connection);
	}

	protected final void addGroup(VisualGroup group) {
		groupAdded(group);
	}

	protected void componentAdded(VisualComponent component) {
	}

	protected void connectionAdded(VisualConnection connection) {
	}

	protected void componentRemoved(VisualComponent component) {
	}

	protected void connectionRemoved(VisualConnection connection) {
	}

	protected void groupAdded(VisualGroup group) {
	}

	protected void groupRemoved(VisualGroup group) {
	}

	public ArrayList<Class<? extends GraphEditorTool>> getAdditionalToolClasses() {
		return new ArrayList<Class<? extends GraphEditorTool>>();
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
		for(VisualNode node : selection)
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
			currentLevel.remove(node);
			group.add(node);
		}

		ArrayList<VisualConnection> connectionsToGroup = new ArrayList<VisualConnection>();
		for(VisualConnection connection : currentLevel.connections)
		{
			if(connection.first.isDescendantOf(group) &&
			   connection.second.isDescendantOf(group))
				connectionsToGroup.add(connection);
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
		for (VisualGroup g: group.groups)
			removeGroup(g);
		for (VisualComponent c: group.components)
			removeComponent(c);

		selection.remove(group);
		group.getParent().remove(group);

		groupRemoved(group);
		// connections will get deleted automatically
	}

	protected void removeComponent(VisualComponent component) {
		for (VisualConnection con : component.getConnections())
			removeConnection(con);
		mathModel.removeComponent(component.refComponent);

		selection.remove(component);
		component.getParent().remove(component);

		refIDToVisualComponentMap.remove(component.getReferencedComponent().getID());
		componentRemoved(component);
	}

	protected void removeConnection(VisualConnection connection) {
		connection.getFirst().removeConnection(connection);
		connection.getSecond().removeConnection(connection);
		mathModel.removeConnection(connection.getReferencedConnection());

		selection.remove(connection);
		connection.getParent().remove(connection);

		refIDToVisualConnectionMap.remove(connection.getReferencedConnection().getID());
		connectionRemoved(connection);
	}

	/**
	 * Deletes the selection.
	 * @author Ivan Poliakov
	 */
	public void deleteSelection() {
		LinkedList<VisualConnection> connectionsToDelete = new LinkedList<VisualConnection>();
		LinkedList<VisualComponent> componentsToDelete = new LinkedList<VisualComponent>();
		LinkedList<VisualGroup> groupsToDelete = new LinkedList<VisualGroup>();

		for (VisualNode node: selection) {
			if (node instanceof VisualGroup)
				groupsToDelete.add((VisualGroup)node);
			else if (node instanceof VisualComponent)
				componentsToDelete.add((VisualComponent)node);
			else if (node instanceof VisualConnection)
				connectionsToDelete.add((VisualConnection)node);
		}

		for (VisualConnection con : connectionsToDelete)
			removeConnection(con);
		for (VisualComponent comp : componentsToDelete)
			removeComponent(comp);
		for (VisualGroup g: groupsToDelete)
			removeGroup(g);
	}

	public void copy(Clipboard clipboard, ClipboardOwner clipboardOwner) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc; DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}

		Element root = doc.createElement("workcraft-clipboard-contents");
		doc.appendChild(root);
		root = doc.getDocumentElement();
		selectionToXML(root);
		clipboard.setContents(new TransferableDocument(doc), clipboardOwner);
	}

	public void paste() {

	}

	public void cut(Clipboard clipboard, ClipboardOwner clipboardOwner) {
		copy(clipboard, clipboardOwner);
		deleteSelection();
	}

	public VisualComponent getComponentByRefID(Integer id) {
		return refIDToVisualComponentMap.get(id);
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
		currentLevel.getParentToLocalTransform().transform(newPoint, newPoint);
		return newPoint;
	}

	public VisualNode hitNode(Point2D pointInRootSpace)
	{
		return currentLevel.hitNode(transformToCurrentSpace(pointInRootSpace));
	}

	public LinkedList<VisualNode> hitObjects(Rectangle2D selectionRect) {
		Point2D min = new Point2D.Double(selectionRect.getMinX(), selectionRect.getMinY());
		Point2D max = new Point2D.Double(selectionRect.getMaxX(), selectionRect.getMaxY());
		min = transformToCurrentSpace(min);
		max = transformToCurrentSpace(max);
		selectionRect.setRect(min.getX(), min.getY(), max.getX()-min.getX(), max.getY()-min.getY());
		return currentLevel.hitObjects(selectionRect);
	}
}