package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
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
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.dom.XMLSerialisation;
import org.workcraft.framework.ComponentFactory;
import org.workcraft.framework.ConnectionFactory;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
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

	private VisualPropertyChangeListener propertyChangeListener = new VisualPropertyChangeListener();

	private MathModel mathModel;
	private VisualGroup root;

	private LinkedList<VisualNode> selection = new LinkedList<VisualNode>();
	private LinkedList<VisualModelEventListener> listeners = new LinkedList<VisualModelEventListener>();

	private HashMap<Integer, VisualComponent> refIDToVisualComponentMap = new HashMap<Integer, VisualComponent>();
	private HashMap<Integer, VisualConnection> refIDToVisualConnectionMap = new HashMap<Integer, VisualConnection>();

	private XMLSerialisation serialiser = new XMLSerialisation();

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

	public VisualModel(MathModel model) throws VisualModelInstantiationException {
		mathModel = model;
		root = new VisualGroup();
		currentLevel = root;

		try {
			// create a default flat structure
			for (Component component : model.getComponents()) {
				VisualComponent visualComponent;
				visualComponent = ComponentFactory.createVisualComponent(component);
				if (visualComponent != null) {
					root.add(visualComponent);
					addComponent(visualComponent);
				}
			}

			for (Connection connection : model.getConnections()) {

				VisualConnection visualConnection = ConnectionFactory.createVisualConnection(connection, this);
				if (visualConnection != null) {
					root.add(visualConnection);
					addConnection(visualConnection);
				}
			}
		} catch (VisualComponentCreationException e) {
			throw new VisualModelInstantiationException ("Failed to create visual component: " + e.getMessage());
		} catch (VisualConnectionCreationException e) {
			throw new VisualModelInstantiationException("Failed to create visual connection:" + e.getMessage());
		}

		addXMLSerialisable();
	}

	public VisualModel(MathModel mathModel, Element visualModelElement) throws VisualModelInstantiationException {
		this.mathModel = mathModel;
		root = new VisualGroup();
		currentLevel = root;

		try {
			Element element = XmlUtil.getChildElement(VisualModel.class.getSimpleName(), visualModelElement);
			pasteFromXML(element, new Point2D.Double(0,0));
		} catch (PasteException e) {
			throw new VisualModelInstantiationException("pasteFromXML failed: " + e.getMessage());
		}

		addXMLSerialisable();
	}

	protected final Collection<VisualNode> pasteFromXML (Element visualElement, Point2D location) throws PasteException {
		List<Element> compElements = XmlUtil.getChildElements("component", visualElement);
		List<Element> conElements = XmlUtil.getChildElements("connection", visualElement);
		List<Element> groupElements = XmlUtil.getChildElements("group", visualElement);

		LinkedList<VisualNode> pasted = new LinkedList<VisualNode>();

		try
		{
			for (Element e: compElements) {
				VisualComponent vcomp = ComponentFactory.createVisualComponent(e, this);
				vcomp.setX(vcomp.getX() + location.getX());
				vcomp.setY(vcomp.getY() + location.getY());
				currentLevel.add(vcomp);
				addComponent(vcomp);

				pasted.add(vcomp);
			}

			for (Element e: groupElements) {
				VisualGroup group = new VisualGroup (e, this);
				group.loadDeferredConnections(this);
				group.setX(group.getX() + location.getX());
				group.setY(group.getY() + location.getY());
				currentLevel.add(group);

				pasted.add(group);
			}

			for (Element e: conElements) {
				VisualConnection vcon = ConnectionFactory.createVisualConnection(e, this);
				currentLevel.add(vcon);

				pasted.add(vcon);
			}

			return pasted;
		} catch (VisualConnectionCreationException e) {
			throw new PasteException ("Cannot create visual connection: " + e.getMessage());

		} catch (VisualComponentCreationException e) {
			throw new PasteException ("Cannot create visual component: " + e.getMessage());
		}
	}

	static void nodesToXML (Element parentElement, Collection <? extends VisualNode> nodes) {
		for (VisualNode node : nodes) {
			if (node instanceof VisualComponent) {
				VisualComponent vc = (VisualComponent)node;
				Element vcompElement = XmlUtil.createChildElement("component", parentElement);
				XmlUtil.writeIntAttr(vcompElement, "ref", vc.getReferencedComponent().getID());
				vc.serialiseToXML(vcompElement);
			} else if (node instanceof VisualConnection) {
				VisualConnection vc = (VisualConnection)node;
				Element vconElement = XmlUtil.createChildElement("connection", parentElement);
				XmlUtil.writeIntAttr(vconElement, "ref", vc.getReferencedConnection().getID());
				vc.serialiseToXML(vconElement);
			} else if (node instanceof VisualGroup) {
				Element childGroupElement = XmlUtil.createChildElement("group", parentElement);
				((VisualGroup)node).serialiseToXML(childGroupElement);
			}
		}
	}

	static void nodesToXML (Element parentElement, Collection <? extends VisualNode> nodes, Point2D offset) {
		Point2D tp = new Point2D.Double();

		for (VisualNode node : nodes) {
			if (node instanceof VisualComponent) {
				VisualComponent vc = (VisualComponent)node;
				tp=vc.getPosition();
				vc.setX(vc.getX()+offset.getX());
				vc.setY(vc.getY()+offset.getY());
				Element vcompElement = XmlUtil.createChildElement("component", parentElement);
				XmlUtil.writeIntAttr(vcompElement, "ref", vc.getReferencedComponent().getID());
				vc.serialiseToXML(vcompElement);
				vc.setX(tp.getX());
				vc.setY(tp.getY());

			} else if (node instanceof VisualConnection) {
				VisualConnection vc = (VisualConnection)node;
				// TODO: do the path points offset?
				//
				Element vconElement = XmlUtil.createChildElement("connection", parentElement);
				XmlUtil.writeIntAttr(vconElement, "ref", vc.getReferencedConnection().getID());
				vc.serialiseToXML(vconElement);
			} else if (node instanceof VisualGroup) {
				Element childGroupElement = XmlUtil.createChildElement("group", parentElement);
				VisualGroup vg = (VisualGroup)node;
				tp=vg.getPosition();
				vg.setX(vg.getX()+offset.getX());
				vg.setY(vg.getY()+offset.getY());
				((VisualGroup)node).serialiseToXML(childGroupElement);
				vg.setX(tp.getX());
				vg.setY(tp.getY());
			}
		}
	}

	private void gatherReferences(Collection<VisualNode> nodes, LinkedList<Component> referencedComponents, LinkedList<Connection> referencedConnections) {
		for (VisualNode n : nodes)
			if (n instanceof VisualComponent)
				referencedComponents.add( ((VisualComponent)n).getReferencedComponent());
			else if (n instanceof VisualConnection)
				referencedConnections.add( ((VisualConnection)n).getReferencedConnection());
			else if (n instanceof VisualGroup)
				gatherReferences( ((VisualGroup)n).getChildren(), referencedComponents, referencedConnections);
	}

	private void selectionToXML(Element xmlElement) {
		Element mathElement = XmlUtil.createChildElement("model", xmlElement);
		XmlUtil.writeStringAttr(mathElement, "class", getMathModel().getClass().getName());
		Element visualElement = XmlUtil.createChildElement("visual-model", xmlElement);

		LinkedList<Component> referencedComponents = new LinkedList<Component>();
		LinkedList<Connection> referencedConnections = new LinkedList<Connection>();

		gatherReferences (selection, referencedComponents, referencedConnections);

		// find the middle? point of the selection
		Rectangle2D selectionBB = new Rectangle2D.Double();
		selectionBB = selection.getFirst().getBoundingBoxInParentSpace();

		for (VisualNode vn: selection) {
			Rectangle2D.union(selectionBB, vn.getBoundingBoxInParentSpace(), selectionBB);
		}
		// offset the elements of the selection
		Point2D offset = new Point2D.Double(-selectionBB.getCenterX(), -selectionBB.getCenterY());

		VisualModel.nodesToXML(visualElement, selection, offset);
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
			l.onSelectionChanged();
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

		VisualGroup group = VisualNode.getCommonParent(first, second);

		group.add(ret);
		addConnection(ret);

		return ret;
	}

	public final void addComponent(VisualComponent component) {
		refIDToVisualComponentMap.put(component.getReferencedComponent().getID(), component);
		component.addListener(propertyChangeListener);

		fireComponentAdded(component);
	}

	private void fireComponentAdded(VisualComponent component) {
		for (VisualModelEventListener l : listeners)
			l.onComponentAdded(component);
	}

	public final void addConnection(VisualConnection connection) {
		connection.getFirst().addConnection(connection);
		connection.getSecond().addConnection(connection);

		if (connection.getReferencedConnection() != null)
			refIDToVisualConnectionMap.put(connection.getReferencedConnection().getID(), connection);

		connection.addListener(propertyChangeListener);

		fireConnectionAdded(connection);
	}

	private void fireConnectionAdded(VisualConnection connection) {
		for (VisualModelEventListener l : listeners)
			l.onConnectionAdded(connection);
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
			if(connection.getFirst().isDescendantOf(group) &&
					connection.getSecond().isDescendantOf(group))
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

		component.removeListener(propertyChangeListener);
		refIDToVisualComponentMap.remove(component.getReferencedComponent().getID());

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

		connection.removeListener(propertyChangeListener);
		refIDToVisualConnectionMap.remove(connection.getReferencedConnection().getID());

		fireConnectionRemoved(connection);
	}

	private void fireConnectionRemoved(VisualConnection connection) {
		for (VisualModelEventListener l : listeners)
			l.onConnectionRemoved(connection);
	}

	private void removeNodes(Collection<VisualNode> nodes) {
		LinkedList<VisualConnection> connectionsToRemove = new LinkedList<VisualConnection>();
		LinkedList<VisualComponent> componentsToRemove = new LinkedList<VisualComponent>();
		LinkedList<VisualGroup> groupsToRemove = new LinkedList<VisualGroup>();

		for (VisualNode n : nodes) {
			if (n instanceof VisualConnection)
				connectionsToRemove.add((VisualConnection)n);
			if (n instanceof VisualComponent)
				componentsToRemove.add((VisualComponent)n);
			if (n instanceof VisualGroup)
				groupsToRemove.add((VisualGroup)n);
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

	/**
	 * @param clipboard
	 * @param clipboardOwner
	 * @author Ivan Poliakov
	 */
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

	public Collection<VisualNode> paste(Clipboard clipboard, Point2D where) throws PasteException {
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
			e.printStackTrace();
		} catch (ModelLoadFailedException e) {
			throw new PasteException (e.getMessage());
		}

		return null;
	}

	public void select(Collection<VisualNode> nodes) {
		selectNone();
		for (VisualNode n: nodes)
			addToSelection(n);
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

	public final void addXMLSerialisable(XMLSerialiser serialisable) {
		serialiser.addSerialiser(serialisable);
	}

	public final void serialiseToXML(Element componentElement) {
		serialiser.serialise(componentElement);
	}
}