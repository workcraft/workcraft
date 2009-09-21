package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.NodeFactory;
import org.workcraft.annotations.MouseListeners;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.DefaultAnchorGenerator;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.NotAnAncestorException;
import org.workcraft.exceptions.PasteException;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.SelectionChangeEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.XmlUtil;

@MouseListeners ({ DefaultAnchorGenerator.class })
public abstract class AbstractVisualModel extends AbstractModel implements VisualModel {
	private Model mathModel;
	private VisualGroup currentLevel;
	private Set<Node> selection = new HashSet<Node>();
	private ObservableStateImpl observableState = new ObservableStateImpl();

	public AbstractVisualModel(VisualGroup root) {
		this (null, root);
	}

	public AbstractVisualModel() {
		this ((Model)null);
	}

	public AbstractVisualModel(Model mathModel) {
		this(mathModel, null);
	}

	public AbstractVisualModel(Model mathModel, VisualGroup root) {
		super(root == null? new VisualGroup() : root);
		this.mathModel = mathModel;

		currentLevel =  (VisualGroup)getRoot();
		new TransformEventPropagator().attach(getRoot());
		new RemovedNodeDeselector(this).attach(getRoot());
	}

	protected final void createDefaultFlatStructure() throws NodeCreationException {
		HashMap <MathNode, VisualComponent> createdNodes = new HashMap <MathNode, VisualComponent>();
		HashMap <VisualConnection, MathConnection> createdConnections = new	HashMap <VisualConnection, MathConnection>();

		for (Node n : mathModel.getRoot().getChildren()) {
			if (n instanceof MathConnection) {
				MathConnection connection = (MathConnection)n;

				// Will create incomplete instance, setConnection() needs to be called later to finalise.
				// This is to avoid cross-reference problems.
				VisualConnection visualConnection = NodeFactory.createVisualConnection(connection);
				createdConnections.put(visualConnection, connection);
			} else {
				MathNode node = (MathNode)n;
				VisualComponent visualComponent = (VisualComponent)NodeFactory.createVisualComponent(node);

				if (visualComponent != null) {
					getRoot().add(visualComponent);
					createdNodes.put(node, visualComponent);
				}
			}
		}

		for (VisualConnection vc : createdConnections.keySet()) {
			MathConnection mc = createdConnections.get(vc);
			vc.setDependencies(createdNodes.get(mc.getFirst()),
					createdNodes.get(mc.getSecond()), mc);
			getRoot().add(vc);
		}
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

	public static Rectangle2D getNodesBoundingBox(Collection<Node> nodes) {
		Rectangle2D selectionBB = null;

		if (nodes.isEmpty()) return selectionBB;

		for (Node vn: nodes) {
			if(vn instanceof Touchable)
				selectionBB = bbUnion(selectionBB, ((Touchable)vn).getBoundingBox());
		}
		return selectionBB;
	}

	public Rectangle2D getSelectionBoundingBox() {
		return getNodesBoundingBox(selection);
	}

	public void transformNodePosition(Collection<Node> nodes, AffineTransform t) {
		assert nodes!=null;
		for (Node node: nodes)
			TransformHelper.applyTransform(node, t);
	}

	public void translateNodes(Collection<Node> nodes, double tx, double ty) {
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
		DrawMan.draw(g, getRoot());
	}

	/**
	 * Get the list of selected objects. Returned list is modifiable!
	 * @return the selection.
	 */
	public Set<Node> selection() {
		return selection;
	}

	private void notifySelectionChanged() {
		sendNotification(new SelectionChangeEvent(this));
	}

	/**
	 * Select all components, connections and groups from the <code>root</code> group.
	 */
	public void selectAll() {
		selection.clear();
		selection.addAll(getRoot().getChildren());

		notifySelectionChanged();
	}

	/**
	 * Clear selection.
	 */
	public void selectNone() {
		if (!selection.isEmpty()) {
			selection.clear();

			notifySelectionChanged();
		}
	}

	private void validateSelection (Node node) {
		if (!Hierarchy.isDescendant(node, getCurrentLevel()))
			throw new RuntimeException ("Cannot select a node that is not in the current editing level");
	}

	private void validateSelection (Collection<Node> nodes) {
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

		validateSelection(nodes);

		selection.clear();
		selection.addAll(nodes);

		notifySelectionChanged();
	}

	public void select(Node node) {
		if (selection.contains(node) && selection.size() == 1)
			return;

		validateSelection(node);

		selection.clear();
		selection.add(node);

		notifySelectionChanged();
	}

	public void addToSelection(Node node) {
		if (selection.contains(node))
			return;

		validateSelection(node);
		selection.add(node);

		notifySelectionChanged();
	}

	public void addToSelection(Collection<Node> nodes) {
		validateSelection(nodes);

		int sizeBefore = selection.size();

		selection.addAll(nodes);

		if (sizeBefore != selection.size())
			notifySelectionChanged();
	}

	public void removeFromSelection(Node node) {
		if (selection.contains(node))
			selection.remove(node);

		notifySelectionChanged();
	}

	public void removeFromSelection(Collection<Node> nodes) {
		int sizeBefore = selection.size();

		selection.removeAll(nodes);

		if (sizeBefore != selection.size())
			notifySelectionChanged();
	}

	public Model getMathModel() {
		return mathModel;
	}

	public VisualModel getVisualModel() {
		return this;
	}

	/**
	 * @return Returns selection ordered the same way as the objects are ordered in the currently active group.
	 */
	public Collection<Node> getSelection() {
		return Collections.unmodifiableSet(selection);
	}

	public Collection<Node> getOrderedCurrentLevelSelection() {
		List<Node> result = new ArrayList<Node>();
		for(Node node : currentLevel.getChildren())
		{
			if(selection.contains(node) && node instanceof VisualNode)
				result.add((VisualNode)node);
		}
		return result;
	}

	@Override
	public Connection connect(Node first, Node second) throws InvalidConnectionException {
		validateConnection(first, second);

		VisualComponent c1 = (VisualComponent) first;
		VisualComponent c2 = (VisualComponent) second;

		MathConnection con = (MathConnection) mathModel.connect(c1.getReferencedComponent(), c2.getReferencedComponent());

		VisualConnection ret = new VisualConnection(con, c1, c2);

		Container group =
			Hierarchy.getNearestAncestor(
					Hierarchy.getCommonParent(first, second),
					Container.class);

		group.add(ret );

		return ret;
	}

	public VisualGroup getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(VisualGroup newCurrentLevel) {
		selection.clear();
		currentLevel = newCurrentLevel;
	}

	private Collection<Node> getGroupableSelection()
	{
		ArrayList<Node> result = new ArrayList<Node>();
		for(Node node : getOrderedCurrentLevelSelection())
			if(node instanceof VisualTransformableNode)
				result.add((VisualTransformableNode)node);
		return result;
	}

	/**
	 * Groups the selection, and selects the newly created group.
	 * @author Arseniy Alekseyev
	 */
	public void groupSelection() {
		Collection<Node> selected = getGroupableSelection();
		if(selected.size() <= 1)
			return;

		VisualGroup group = new VisualGroup();

		currentLevel.add(group);

		currentLevel.reparent(selected, group);

		ArrayList<Node> connectionsToGroup = new ArrayList<Node>();

		for(VisualConnection connection : Hierarchy.getChildrenOfType(currentLevel, VisualConnection.class))
		{
			if(connection.getFirst().isDescendantOf(group) &&
					connection.getSecond().isDescendantOf(group)) {
				connectionsToGroup.add(connection);
			}
		}

		currentLevel.reparent(connectionsToGroup, group);

		select(group);
	}

	/**
	 * Ungroups all groups in the current selection and adds the ungrouped components to the selection.
	 * @author Arseniy Alekseyev
	 */
	public void ungroupSelection() {
		ArrayList<Node> toSelect = new ArrayList<Node>();

		for(Node node : getOrderedCurrentLevelSelection())
		{
			if(node instanceof VisualGroup)
			{
				VisualGroup group = (VisualGroup)node;
				for(Node subNode : group.unGroup())
					toSelect.add(subNode);
				currentLevel.remove(group);
			}
			else
				toSelect.add(node);
		}

		select(toSelect);
	}

	/*protected void removeGroup(VisualGroup group) {
		removeNodes(group.getChildren());

		((Container)group.getParent()).remove(group);
		selection.remove(group);
	}*/


	/**
	 * Deletes the selection.
	 * @author Ivan Poliakov
	 */
	public void deleteSelection() {
		//System.out.println ("Deleting selection (" + selection.size()+" nodes)");
		HashMap<Container, LinkedList<Node>> batches = new HashMap<Container, LinkedList<Node>>();
		LinkedList<Node> remainingSelection = new LinkedList<Node>();


		for (Node n : selection) {
			if (n.getParent() instanceof Container) {
				Container c = (Container)n.getParent();
				LinkedList<Node> batch = batches.get(c);
				if (batch == null) {
					batch = new LinkedList<Node>();
					batches.put(c, batch);
				}
				batch.add(n);
			} else remainingSelection.add(n);
		}

		for (Container c : batches.keySet())
			c.remove(batches.get(c));

		select(remainingSelection);
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

	public Collection<Node> paste(Collection<Node> what, Point2D where) throws PasteException {
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



	public void cut(Clipboard clipboard, ClipboardOwner clipboardOwner) throws ParserConfigurationException {
		copy(clipboard, clipboardOwner);
		deleteSelection();
	}

	//	public VisualComponent getComponentByRefID(Integer id) {
	//		return refIDToVisualComponentMap.get(id);
	//	}


	private Point2D transformToCurrentSpace(Point2D pointInRootSpace)
	{
		if(currentLevel == getRoot())
			return pointInRootSpace;
		Point2D newPoint = new Point2D.Double();
		try {
			currentLevel.getAncestorToParentTransform((VisualGroup)getRoot()).transform(pointInRootSpace, newPoint);
		} catch (NotAnAncestorException e) {
			e.printStackTrace();
			throw new RuntimeException("Root is not an ancestor of the current node o_O");
		}
		return newPoint;
	}

	public VisualNode hitTest(Point2D pointInRootSpace)
	{
		return (VisualNode) HitMan.hitTestForSelection(transformToCurrentSpace(pointInRootSpace), currentLevel);
	}

	public LinkedList<Node> boxHitTest(Point2D p1, Point2D p2) {
		p1 = transformToCurrentSpace(p1);
		p2 = transformToCurrentSpace(p2);
		return currentLevel.hitObjects(p1, p2);
	}

	public LinkedList<Node> boxHitTest(Rectangle2D selectionRect) {
		Point2D min = new Point2D.Double(selectionRect.getMinX(), selectionRect.getMinY());
		Point2D max = new Point2D.Double(selectionRect.getMaxX(), selectionRect.getMaxY());
		min = transformToCurrentSpace(min);
		max = transformToCurrentSpace(max);
		selectionRect.setRect(min.getX(), min.getY(), max.getX()-min.getX(), max.getY()-min.getY());

		return currentLevel.hitObjects(selectionRect);
	}

	public void addObserver(StateObserver obs) {
		observableState.addObserver(obs);
	}

	public void removeObserver(StateObserver obs) {
		observableState.removeObserver(obs);
	}

	public void sendNotification(StateEvent e) {
		observableState.sendNotification(e);
	}

}
