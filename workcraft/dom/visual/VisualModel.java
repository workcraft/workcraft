package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.exceptions.VisualClassConstructionException;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.gui.edit.graph.GraphEditorPane;

public class VisualModel {
	protected MathModel model;
	protected VisualComponentGroup root;
	protected GraphEditorPane editor = null;

	protected LinkedList<Selectable> selection = new LinkedList<Selectable>();

	public VisualModel(MathModel model) throws VisualClassConstructionException {
		this.model = model;
		root = new VisualComponentGroup();

		// create a default flat structure
		for (Component component : model.getComponents()) {
			VisualComponent visualComponent = (VisualComponent)PluginManager.createVisualClassFor(component, VisualComponent.class);
			if (visualComponent != null)
				root.add(visualComponent);
		}

		for (Connection connection : model.getConnections()) {
			VisualConnection visualConnection = (VisualConnection)PluginManager.createVisualClassFor(connection, VisualConnection.class);
			if (visualConnection != null)
				root.add(visualConnection);
		}
	}

	public VisualModel(MathModel model, Element visualElement) throws VisualClassConstructionException {
		this.model = model;

		// load structure from XML
		NodeList nodes = visualElement.getElementsByTagName("group");

		if (nodes.getLength() != 1)
			throw new VisualClassConstructionException ("<visual> section of the document must contain one, and only one root group");

		root = new VisualComponentGroup ((Element)nodes.item(0), model);
	}

	public void toXML(Element xmlVisualElement) {
		// create root group element
		Element rootGroupElement = xmlVisualElement.getOwnerDocument().createElement("group");
		root.toXML(rootGroupElement);
		xmlVisualElement.appendChild(rootGroupElement);
	}

	private void drawSelection(Graphics2D g) {
		g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));
		Rectangle2D.Double rect = null;
		for(Selectable vo : selection) {
			if(vo==null)
				continue;
			Rectangle2D bb = vo.getBoundingBox();
			if(rect==null) {
				rect = new Rectangle2D.Double();
				rect.setRect(bb);
			}
			else
				rect.add(bb);
			if(vo instanceof VisualConnection)
				continue; // TODO somehow show selected connections
			g.setColor(new Color(255, 0, 0, 64));
			g.fill(bb);
			g.setColor(new Color(255, 0, 0));
			g.draw(bb);
		}
		if(rect!=null) {
			g.setColor(new Color(255, 0, 0, 128));
			g.draw(rect);
		}
	}

	public void draw (Graphics2D g) {
		root.draw(g);
		drawSelection(g);
	}

	public VisualComponentGroup getRoot() {
		return root;
	}

	public void setEditorPane(GraphEditorPane editor) {
		this.editor = editor;
		if(editor.getDocument()!=this) {
			editor.setDocument(this);
		}
	}

	public GraphEditorPane getEditorPane() {
		return editor;
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
}