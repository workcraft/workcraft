package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.util.LinkedList;

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

public class VisualModel implements Plugin, Model {
	protected MathModel mathModel;
	protected VisualComponentGroup root;

	protected LinkedList<Selectable> selection = new LinkedList<Selectable>();

	protected LinkedList<VisualModelListener> listeners;

	public VisualModel(MathModel model) throws VisualModelConstructionException {
		mathModel = model;
		root = new VisualComponentGroup(null);
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
	public LinkedList<Selectable> selection() {
		return selection;
	}

	/**
	 * Select all components, connections and groups from the <code>root</code> group.
	 */
	public void selectAll() {
		selection.clear();
		selection.addAll(root.components);
		selection.addAll(root.connections);
		selection.addAll(root.childGroups);
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
	public boolean isObjectSelected(Selectable so) {
		return selection.contains(so);
	}

	/**
	 * Add an object to the selection if it is not already selected.
	 * @param so an object to select
	 */
	public void addToSelection(Selectable so) {
		if(!isObjectSelected(so))
			selection.add(so);
	}

	/**
	 * Remove an object from the selection if it is selected.
	 * @param so an object to deselect.
	 */
	public void removeFromSelection(Selectable so) {
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

	public Selectable[] getSelection() {
		return selection.toArray(new Selectable[0]);
	}

	public VisualConnection connect(VisualComponent first, VisualComponent second) throws InvalidConnectionException {
		Connection con = mathModel.connect(first.getReferencedComponent(), second.getReferencedComponent());
		VisualConnection ret = new VisualConnection(con, first, second);
		root.add(ret);

		fireModelStructureChanged();

		return ret;
	}

}