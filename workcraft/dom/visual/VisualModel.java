package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.MathModelListener;
import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.gui.edit.graph.GraphEditorPane;

public class VisualModel implements Plugin, Model {
	protected MathModel mathModel;
	protected VisualComponentGroup root;

	protected LinkedList<Selectable> selection = new LinkedList<Selectable>();

	protected LinkedList<VisualModelListener> listeners;

	public VisualModel(MathModel model) throws VisualModelConstructionException {
		this.mathModel = model;
		this.root = new VisualComponentGroup();
		this.listeners = new LinkedList<VisualModelListener>();

		// create a default flat structure
		for (Component component : model.getComponents()) {
			VisualComponent visualComponent = (VisualComponent)PluginManager.createVisualClassFor(component, VisualComponent.class);
			if (visualComponent != null)
				this.root.add(visualComponent);
		}

		for (Connection connection : model.getConnections()) {
			VisualConnection visualConnection = (VisualConnection)PluginManager.createVisualClassFor(connection, VisualConnection.class);
			if (visualConnection != null)
				this.root.add(visualConnection);
		}
	}

	public VisualModel(MathModel mathModel, Element visualElement) throws VisualModelConstructionException {
		this.mathModel = mathModel;

		// load structure from XML
		NodeList nodes = visualElement.getElementsByTagName("group");

		if (nodes.getLength() != 1)
			throw new VisualModelConstructionException ("<visual-model> section of the document must contain one, and only one root group");

		this.root = new VisualComponentGroup ((Element)nodes.item(0), mathModel);
	}

	public void toXML(Element xmlVisualElement) {
		// create root group element
		Element rootGroupElement = xmlVisualElement.getOwnerDocument().createElement("group");
		this.root.toXML(rootGroupElement);
		xmlVisualElement.appendChild(rootGroupElement);
	}

	public void draw (Graphics2D g) {
		this.root.draw(g);
	}

	public VisualComponentGroup getRoot() {
		return this.root;
	}

	/**
	 * Get the list of selected objects. Returned list is modifiable!
	 * @return the selection.
	 */
	public LinkedList<Selectable> selection() {
		return this.selection;
	}

	/**
	 * Select all components, connections and groups from the <code>root</code> group.
	 */
	public void selectAll() {
		this.selection.clear();
		this.selection.addAll(this.root.components);
		this.selection.addAll(this.root.connections);
		this.selection.addAll(this.root.childGroups);
	}

	/**
	 * Clear selection.
	 */
	public void selectNone() {
		this.selection.clear();
	}

	/**
	 * Check if the object is selected.<br/>
	 * <i>Important!</i> Slow function. It searches through all the selected objects,
	 * so it should not be called frequently.
	 * @param so selectable object
	 * @return if <code>so</code> is selected
	 */
	public boolean isObjectSelected(Selectable so) {
		return this.selection.contains(so);
	}

	/**
	 * Add an object to the selection if it is not already selected.
	 * @param so an object to select
	 */
	public void addToSelection(Selectable so) {
		if(!isObjectSelected(so))
			this.selection.add(so);
	}

	/**
	 * Remove an object from the selection if it is selected.
	 * @param so an object to deselect.
	 */
	public void removeFromSelection(Selectable so) {
		this.selection.remove(so);
	}


	public MathModel getMathModel() {
		return this.mathModel;
	}


	public VisualModel getVisualModel() {
		return this;
	}


	public String getTitle() {
		return this.mathModel.getTitle();
	}


	public String getDisplayName() {
		return this.mathModel.getDisplayName();
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

	public void addListener(MathModelListener listener) {
		mathModel.addListener(listener);
	}

	public void removeListener (MathModelListener listener) {
		mathModel.removeListener(listener);
	}

	public List<Selectable> getSelection() {
		return selection;
	}

}