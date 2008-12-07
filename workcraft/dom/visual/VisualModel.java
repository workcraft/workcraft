package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.gui.edit.graph.GraphEditorPane;

public class VisualModel implements Plugin, Model {
	protected MathModel mathModel;
	protected VisualComponentGroup root;
	protected GraphEditorPane editor = null;

	protected LinkedList<Selectable> selection = new LinkedList<Selectable>();

	public VisualModel(MathModel model) throws VisualModelConstructionException {
		this.mathModel = model;
		this.root = new VisualComponentGroup();

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

	private void drawSelection(Graphics2D g) {
		g.setStroke(new BasicStroke((float) this.editor.getViewport().pixelSizeInUserSpace().getX()));
		Rectangle2D.Double rect = null;
		for(Selectable vo : this.selection) {
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
		this.root.draw(g);
		drawSelection(g);
	}

	public VisualComponentGroup getRoot() {
		return this.root;
	}

	public void setEditorPane(GraphEditorPane editor) {
		this.editor = editor;
		if(editor.getModel()!=this)
			editor.setModel(this);
	}

	public GraphEditorPane getEditorPane() {
		return this.editor;
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
}