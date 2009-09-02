package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.IntIdentifiable;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.MathModelListener;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionAnchorPoint;
import org.workcraft.framework.ComponentFactory;
import org.workcraft.framework.ConnectionFactory;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.framework.exceptions.PasteException;
import org.workcraft.framework.exceptions.VisualComponentCreationException;
import org.workcraft.framework.exceptions.VisualConnectionCreationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.framework.serialisation.xml.NoAutoSerialisation;
import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.util.Hierarchy;
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

	public class ModelListener implements MathModelListener {
		public void onNodePropertyChanged(String propertyName, MathNode n) {
			if (n instanceof Component)
				fireComponentPropertyChanged(propertyName, (VisualComponent)getVisualComponentByID( ((Component)n).getID()));
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
		}

		@Override
		public void onSelectionChanged(Collection<HierarchyNode> selection) {
			// TODO Auto-generated method stub

		}
	}

	private VisualPropertyChangeListener propertyChangeListener = new VisualPropertyChangeListener();

	private MathModel mathModel = null;
	private VisualGroup root = new VisualGroup();
	VisualGroup currentLevel = root;

	private Set<HierarchyNode> selection = new HashSet<HierarchyNode>();
	private LinkedList<VisualModelEventListener> listeners = new LinkedList<VisualModelEventListener>();
	private HashMap<Integer, HierarchyNode> visualComponents = new HashMap<Integer, HierarchyNode>();
	private ModelListener mathModelListener = new ModelListener();

	protected final void createDefaultFlatStructure() throws VisualComponentCreationException, VisualConnectionCreationException {
		for (Component component : mathModel.getComponents()) {
			VisualNode visualComponent;
			visualComponent = ComponentFactory.createVisualComponent(component);

			if (visualComponent != null) {
				root.add(visualComponent);
				registerNode(visualComponent);
			}
		}

		ReferenceResolver refRes = createMathIDtoVisualObjectResolver();

		for (Connection connection : mathModel.getConnections()) {
			VisualConnection visualConnection = ConnectionFactory.createVisualConnection(connection, refRes);

			Hierarchy.getNearestAncestor(
					Hierarchy.getCommonParent(visualConnection.getFirst(), visualConnection.getSecond()),
					Container.class).add(visualConnection);
			registerNode(visualConnection);

		}
	}

	public VisualModel(MathModel model) throws VisualModelInstantiationException {
		setMathModel(model);
	}

	/*private void gatherReferences(Collection<HierarchyNode> nodes, HashSet<MathNode> referenceds) {
		for (HierarchyNode n : nodes)
			if(n instanceof DependentNode)
				referenceds.addAll(((DependentNode)n).getMathReferences());
	}*/

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

	public void transformNodePosition(Collection<HierarchyNode> nodes, AffineTransform t) {
		assert nodes!=null;
		for (HierarchyNode node: nodes)
			TransformHelper.applyTransform(node, t);
	}

	public void translateNodes(Collection<HierarchyNode> nodes, double tx, double ty) {
		AffineTransform t = AffineTransform.getTranslateInstance(tx, ty);

		transformNodePosition(nodes, t);
	}

	public void translateSelection(double tx, double ty) {
		translateNodes(selection, tx, ty);
	}

	public void scaleSelection(double sx, double sy) {
		Rectangle2D selectionBB = getSelectionBoundingBox();

		AffineTransform t = new AffineTransform();

		t.translate(selectionBB.getCenterX(), selectionBB.getCenterY());
		t.scale(sx, sy);
		t.translate(-selectionBB.getCenterX(), -selectionBB.getCenterY());

		transformNodePosition(selection, t);
	}

	public void rotateSelection(double theta) {
		Rectangle2D selectionBB = getSelectionBoundingBox();

		AffineTransform t = new AffineTransform();

		t.translate(selectionBB.getCenterX(), selectionBB.getCenterY());
		t.rotate(theta);
		t.translate(-selectionBB.getCenterX(), -selectionBB.getCenterY());

		transformNodePosition(selection, t);
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
	public Set<HierarchyNode> selection() {
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

	public boolean isObjectSelected(HierarchyNode so) {
		return selection.contains(so);
	}

	public void addToSelection(HierarchyNode so) {
		selection.add(so);
	}

	public void removeFromSelection(HierarchyNode so) {
		selection.remove(so);
	}

	@NoAutoSerialisation
	public void setMathModel(MathModel mathModel) {
		if (this.mathModel != null)
			this.mathModel.removeListener(mathModelListener);
		this.mathModel = mathModel;
		mathModel.addListener(mathModelListener);
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

	/**
	 * @return Returns selection ordered the same way as the objects are ordered in the currently active group.
	 */
	public List<HierarchyNode> getSelection() {
		List<HierarchyNode> result = new ArrayList<HierarchyNode>();
		for(HierarchyNode node : currentLevel.getChildren())
		{
			if(selection.contains(node))
			result.add(node);
		}
		return result;
	}

	public void validateConnection(HierarchyNode first, HierarchyNode second) throws InvalidConnectionException {
		if (first instanceof VisualComponent && second instanceof VisualComponent) {
			mathModel.validateConnection(new Connection (((VisualComponent)first).getReferencedComponent(),
					((VisualComponent)second).getReferencedComponent()));
		}
		else throw new InvalidConnectionException("Only connections between components are allowed");
	}

	public VisualConnection connect(HierarchyNode first, HierarchyNode second) throws InvalidConnectionException {
		validateConnection(first, second);

		VisualComponent firstComponent = (VisualComponent)first;
		VisualComponent secondComponent = (VisualComponent)second;

		Connection con = mathModel.connect(firstComponent.getReferencedComponent(), secondComponent.getReferencedComponent());
		VisualConnection ret = new VisualConnection(con, firstComponent, secondComponent);

		Container group =
			Hierarchy.getNearestAncestor(
			Hierarchy.getCommonParent(first, second),
			Container.class);

		group.add(ret);
		registerNode(ret);

		return ret;
	}

	public final void registerNode(HierarchyNode node) {
		if(node instanceof PropertyEditable)
			((PropertyEditable)node).addPropertyChangeListener(propertyChangeListener);

		if(node instanceof IntIdentifiable)
		{
			int id = getNextNodeID();
			((IntIdentifiable)node).setID(id);
			visualComponents.put(id, node);
		}

		for(HierarchyNode subnode : node.getChildren())
				registerNode(subnode);

		fireNodeAdded(node);
	}

	private int nodeIDCounter = 0;
	private int getNextNodeID() {
		return 	nodeIDCounter++;
	}

	private void fireNodeAdded(HierarchyNode component) {
		if(component instanceof VisualComponent)
			for (VisualModelEventListener l : listeners)
				l.onComponentAdded((VisualComponent)component);

		if(component instanceof VisualConnection)
			for (VisualModelEventListener l : listeners)
				l.onConnectionAdded((VisualConnection)component);
	}

	public ArrayList<GraphEditorTool> getAdditionalTools() {
		return new ArrayList<GraphEditorTool>();
	}

	public void clearColorisation() {
		root.clearColorisation();
	}



	public VisualGroup getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(VisualGroup newCurrentLevel) {
		selection.clear();
		currentLevel = newCurrentLevel;
		fireSelectionChanged();
	}

	private Collection<HierarchyNode> getGroupableSelection()
	{
		ArrayList<HierarchyNode> result = new ArrayList<HierarchyNode>();
		for(HierarchyNode node : getSelection())
			if(node instanceof VisualTransformableNode)
				result.add((VisualTransformableNode)node);
		return result;
	}

	/**
	 * Groups the selection, and selects the newly created group.
	 * @author Arseniy Alekseyev
	 */
	public void groupSelection() {
		Collection<HierarchyNode> selected = getGroupableSelection();
		if(selected.size() <= 1)
			return;
		VisualGroup group = new VisualGroup();
		currentLevel.add(group);
		for(HierarchyNode node : selected)
		{
			currentLevel.remove(node);
			group.add(node);
		}

		ArrayList<VisualConnection> connectionsToGroup = new ArrayList<VisualConnection>();
		for(VisualConnection connection : NodeHelper.getChildrenOfType(currentLevel, VisualConnection.class))
		{
			if(connection.getFirst().isDescendantOf(group) &&
					connection.getSecond().isDescendantOf(group)) {
				connectionsToGroup.add(connection);
			}
		}

		for(VisualConnection connection : connectionsToGroup)
		{
			currentLevel.remove(connection);
			group.add(connection);
		}

		selection.clear();
		selection.add(group);
		fireSelectionChanged();
	}

	/**
	 * Ungroups all groups in the current selection and adds the ungrouped components to the selection.
	 * @author Arseniy Alekseyev
	 */
	public void ungroupSelection() {
		ArrayList<HierarchyNode> toSelect = new ArrayList<HierarchyNode>();

		for(HierarchyNode node : getSelection())
		{
			if(node instanceof VisualGroup)
			{
				VisualGroup group = (VisualGroup)node;
				for(HierarchyNode subNode : group.unGroup())
					toSelect.add(subNode);
				currentLevel.remove(group);
			}
			else
				toSelect.add(node);
		}

		selection.clear();

		for(HierarchyNode node : toSelect)
			selection.add(node);
		fireSelectionChanged();
	}

	protected void removeGroup(VisualGroup group) {
		removeNodes(group.getChildren());

		((Container)group.getParent()).remove(group);
		selection.remove(group);
	}

	protected void removeComponent(VisualComponent component) {
		for (VisualConnection con : component.getConnections())
			removeConnection(con);
		mathModel.removeComponent(component.getReferencedComponent());

		selection.remove(component);
		((Container)component.getParent()).remove(component);

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

		((Container)connection.getParent()).remove(connection);
		selection.remove(connection);

		connection.removePropertyChangeListener(propertyChangeListener);
		//refIDToVisualConnectionMap.remove(connection.getReferencedConnection().getID());

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
		//selectionToXML(root);
		clipboard.setContents(new TransferableDocument(doc), clipboardOwner);
	}

	public Collection<HierarchyNode> paste(Collection<HierarchyNode> what, Point2D where) throws PasteException {
		/*try {
			Document doc = (Document)clipboard.getData(TransferableDocument.DOCUMENT_FLAVOR);

			Element root = doc.getDocumentElement();
			if (!root.getTagName().equals("workcraft-clipboard-contents"))
				return null;

			Element mathModelElement = XmlUtil.getChildElement("model", root);
			Element visualModelElement = XmlUtil.getChildElement("visual-model", root);

			if (mathModelElement == null || visualModelElement == null)
				throw new PasteException("Structure of clipboard XML is invalid.");

			mathModel.pasteFromXML(mathModelElement);
			//return pasteFromXML(visualModelElement, where);
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
			throw new PasteException (e);
		} catch (LoadFromXMLException e) {
			throw new PasteException (e);
		}*/

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

	public HierarchyNode getVisualComponentByID(Integer id) {
		return visualComponents.get(id);
	}

	public HashSet<HierarchyNode> getVisualComponents() {
		return new HashSet<HierarchyNode>(visualComponents.values());
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

	protected VisualPropertyChangeListener getPropertyChangeListener() {
		return propertyChangeListener;
	}

	public ReferenceResolver createMathIDtoVisualObjectResolver() {
		return new ReferenceResolver() {
			private HashMap<String, LinkedList<VisualNode>> map = new HashMap<String, LinkedList<VisualNode>>();

			private void process(HierarchyNode node) {
				if(node instanceof DependentNode && node instanceof VisualNode)
					for (MathNode mn : ((DependentNode)node).getMathReferences())
					{
						String id = Integer.toString(mn.getID());
						LinkedList<VisualNode> list = map.get(id);
						if (list == null) {
							list = new LinkedList<VisualNode>();
							map.put(id, list);
						}
						list.add((VisualNode)node);
					}

				for (HierarchyNode cn : node.getChildren())
					process(cn);
			}

			{
				process(root);
			}

			public Object getObject(String reference) {
				return map.get(reference);
			}
		};
	}

	public void setRoot(HierarchyNode root) {
		if (root instanceof VisualGroup)
		{
			this.root = (VisualGroup)root;
			//TODO: unregister old root
		}
		else
			throw new RuntimeException("The root node of a visual model must be a visual group.");
	}
}