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
import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.gui.edit.graph.GraphEditorPane;

public class VisualAbstractGraphModel {
	protected AbstractGraphModel model;
	protected VisualComponentGroup root;
	protected GraphEditorPane editor = null;

	protected LinkedList<Selectable> selection = new LinkedList<Selectable>();

	protected static Object createVisualClassFor (Object object, Class<?> expectedClass) throws VisualModelConstructionException {
		// Find the corresponding visual class
		VisualClass vcat = object.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(object.getClass());
			Object visual = ctor.newInstance(object);

			if (!expectedClass.isAssignableFrom(visual.getClass()))
				throw new VisualModelConstructionException ("visual class " + visual.getClass().getName() +
						", created for object of class " + object.getClass().getName() + ", is not inherited from "
						+ expectedClass.getName());

			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be loaded for class " + object.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualModelConstructionException("visual class " + vcat.value() +
					" does not declare the required constructor " + vcat.value() +
					"(" + object.getClass().getName() +")" );
		} catch (IllegalArgumentException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: " + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}

	protected static Object createVisualClassFor (Object object, Class<?> expectedClass, Element xmlElement) throws VisualModelConstructionException {
		// Find the corresponding visual class
		VisualClass vcat = object.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(object.getClass(), Element.class);
			Object visual = ctor.newInstance(object, xmlElement);

			if (!expectedClass.isAssignableFrom(visual.getClass()))
				throw new VisualModelConstructionException ("visual class " + visual.getClass().getName() +
						", created for object of class " + object.getClass().getName() + ", is not inherited from "
						+ expectedClass.getName());

			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be loaded for class " + object.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualModelConstructionException("visual class " + vcat.value() +
					" does not declare the required constructor " + vcat.value() +
					"(" + object.getClass().getName() + ", " + Element.class.getName()+")" );
		} catch (IllegalArgumentException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: " + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}

	public VisualAbstractGraphModel(AbstractGraphModel model) throws VisualModelConstructionException {
		this.model = model;
		root = new VisualComponentGroup();

		// create a default flat structure
		for (Component component : model.getComponents()) {
			VisualComponent visualComponent = (VisualComponent)createVisualClassFor(component, VisualComponent.class);
			if (visualComponent != null)
				root.add(visualComponent);
		}

		for (Connection connection : model.getConnections()) {
			VisualConnection visualConnection = (VisualConnection)createVisualClassFor(connection, VisualConnection.class);
			if (visualConnection != null)
				root.add(visualConnection);
		}
	}

	public VisualAbstractGraphModel(AbstractGraphModel model, Element visualElement) throws VisualModelConstructionException {
		this.model = model;

		// load structure from XML
		NodeList nodes = visualElement.getElementsByTagName("group");

		if (nodes.getLength() != 1)
			throw new VisualModelConstructionException ("<visual> section of the document must contain one, and only one root group");

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