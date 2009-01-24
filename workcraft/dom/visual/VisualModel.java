package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.MathModelListener;
import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.gui.edit.tools.GraphEditorTool;

public class VisualModel implements Plugin, Model {
	protected MathModel mathModel;
	protected VisualComponentGroup root;

	protected LinkedList<VisualNode> selection = new LinkedList<VisualNode>();

	protected LinkedList<VisualModelListener> listeners;

	public VisualModel(MathModel model) throws VisualModelConstructionException {
		mathModel = model;
		root = new VisualComponentGroup(null);
		currentLevel = root;
		listeners = new LinkedList<VisualModelListener>();

		// create a default flat structure
		for (Component component : model.getComponents()) {
			VisualComponent visualComponent = (VisualComponent)PluginManager.createVisualComponent(component, root);
			if (visualComponent != null)
				root.add(visualComponent);
		}

		for (Connection connection : model.getConnections()) {
			VisualConnection visualConnection = (VisualConnection)PluginManager.createVisualComponent(connection, root);
			if (visualConnection != null)
				root.add(visualConnection);
		}
	}

	public VisualModel(MathModel mathModel, Element visualElement) throws VisualModelConstructionException {
		this(mathModel);

		// load structure from XML
		NodeList nodes = visualElement.getElementsByTagName("group");

		if (nodes.getLength() != 1)
			throw new VisualModelConstructionException ("<visual-model> section of the document must contain one, and only one root group");

		root = new VisualComponentGroup ((Element)nodes.item(0), mathModel, null);
	}

	public void toXML(Element xmlVisualElement) {
		// create root group element
		Element rootGroupElement = xmlVisualElement.getOwnerDocument().createElement("group");
		root.toXML(rootGroupElement);
		xmlVisualElement.appendChild(rootGroupElement);
	}

	public void draw (Graphics2D g) {
		root.draw(g);
	}

	public VisualComponentGroup getRoot() {
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
	public void selectAll() {
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
		VisualConnection ret = new VisualConnection(con, first, second, root);
		first.addConnection(ret);
		second.addConnection(ret);
		root.add(ret);

		fireModelStructureChanged();

		return ret;
	}

	public ArrayList<Class<? extends GraphEditorTool>> getAdditionalToolClasses() {
		return new ArrayList<Class<? extends GraphEditorTool>>();
	}

	public void clearColorisation() {
		root.clearColorisation();
	}

	VisualComponentGroup currentLevel;

	public VisualComponentGroup getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(VisualComponentGroup newCurrentLevel) {
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
	public void group() {
		List<VisualTransformableNode> selected = getTransformableSelection();
		if(selected.size() <= 1)
			return;
		VisualComponentGroup group = new VisualComponentGroup(currentLevel);
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
	public void ungroup() {
		ArrayList<VisualNode> unGrouped = new ArrayList<VisualNode>();

		for(VisualNode node : getSelection())
		{
			if(node instanceof VisualComponentGroup)
			{
				VisualComponentGroup group = (VisualComponentGroup)node;
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

	protected void deleteGroup(VisualComponentGroup group) {



		for (VisualComponentGroup g: group.groups)
			deleteGroup(g);
		for (VisualComponent c: group.components)
			deleteComponent(c);

		selection.remove(group);
		group.getParent().remove(group);

		// connections will get deleted automatically
	}

	protected void deleteComponent(VisualComponent component) {
		for (VisualConnection con : component.getConnections())
			deleteConnection(con);
		mathModel.removeComponent(component.refComponent);

		selection.remove(component);
		component.getParent().remove(component);
	}

	protected void deleteConnection(VisualConnection connection) {
		connection.first.removeConnection(connection);
		connection.second.removeConnection(connection);
		mathModel.removeConnection(connection.getReferencedConnection());

		selection.remove(connection);
		connection.getParent().remove(connection);
	}

	/**
	 * Deletes the selection.
	 * @author Ivan Poliakov
	 */
	public void delete() {
		LinkedList<VisualConnection> connectionsToDelete = new LinkedList<VisualConnection>();
		LinkedList<VisualComponent> componentsToDelete = new LinkedList<VisualComponent>();
		LinkedList<VisualComponentGroup> groupsToDelete = new LinkedList<VisualComponentGroup>();

		for (VisualNode node: selection) {
			if (node instanceof VisualComponentGroup)
				groupsToDelete.add((VisualComponentGroup)node);
			else if (node instanceof VisualComponent)
				componentsToDelete.add((VisualComponent)node);
			else if (node instanceof VisualConnection)
				connectionsToDelete.add((VisualConnection)node);
		}

		for (VisualConnection con : connectionsToDelete)
			deleteConnection(con);
		for (VisualComponent comp : componentsToDelete)
			deleteComponent(comp);
		for (VisualComponentGroup g: groupsToDelete)
			deleteGroup(g);
	}
}