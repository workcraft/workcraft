package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathModel;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.util.XmlUtil;


public class VisualComponentGroup extends VisualNode {
	protected VisualComponentGroup parent = null;

	protected Set<VisualComponentGroup> childGroups;
	protected Set<VisualComponent> components;
	protected Set<VisualConnection> connections;

	public VisualComponentGroup (VisualComponentGroup parent) {
		super(parent);
		childGroups = new HashSet<VisualComponentGroup>();
		components = new HashSet<VisualComponent>();
		connections = new HashSet<VisualConnection>();
	}

	public VisualComponentGroup (Element element, MathModel refModel, VisualComponentGroup parent) throws VisualModelConstructionException {
		this(parent);

		NodeList nodes = element.getElementsByTagName("component");

		for (int i=0; i<nodes.getLength(); i++) {
			Element vcompElement = (Element)nodes.item(i);
			int ref = XmlUtil.readIntAttr(vcompElement, "ref", -1);
			Component refComponent = refModel.getComponentByID(ref);
			if (refComponent == null)
				throw new VisualModelConstructionException ("a visual component references to the model component with ID=" +
						vcompElement.getAttribute("ref")+", which was not found");
			VisualComponent visualComponent = (VisualComponent)PluginManager.createVisualComponent(refComponent, vcompElement, parent);
			if (visualComponent == null)
				throw new VisualModelConstructionException ("a visual component references to the model component with ID=" +
						vcompElement.getAttribute("ref")+", which does not declare a visual represenation class");
			add(visualComponent);
		}

		nodes = element.getElementsByTagName("connection");

		for (int i=0; i<nodes.getLength(); i++) {
			Element vconElement = (Element)nodes.item(i);
			int ref = XmlUtil.readIntAttr(vconElement, "ref", -1);
			Connection refConnection = refModel.getConnectionByID(ref);
			if (refConnection == null)
				throw new VisualModelConstructionException ("a visual component references to the model component with ID=" +
						vconElement.getAttribute("ref")+", which was not found");
			VisualConnection visualConnection = (VisualConnection)PluginManager.createVisualComponent(refConnection, vconElement, parent);
			if (visualConnection == null)
				throw new VisualModelConstructionException ("a visual component references to the model component with ID=" +
						vconElement.getAttribute("ref")+", which does not declare a visual represenation class");
			add(visualConnection);
		}

		nodes = element.getElementsByTagName("group");

		for (int i=0; i<nodes.getLength(); i++) {
			Element groupElement = (Element)nodes.item(i);
			add (new VisualComponentGroup (groupElement, refModel, this));
		}
	}

	@Override
	public void draw(Graphics2D g) {
		AffineTransform rest1 = g.getTransform();

		// Apply group transform
		g.transform(transform);

		for (VisualConnection connection : connections) {
			AffineTransform rest2 = g.getTransform();
			connection.draw(g);
			g.setTransform(rest2);
		}


		for (VisualComponentGroup group : childGroups) {
			AffineTransform rest2 = g.getTransform();
			g.transform(group.transform);
			group.draw(g);
			// Restore group transform
			g.setTransform(rest2);
		}

		for (VisualComponent component : components) {
			AffineTransform rest2 = g.getTransform();
			g.transform(component.transform);
			component.draw(g);
			// Restore group transform
			g.setTransform(rest2);
		}

		// Restore original transform
		g.setTransform(rest1);
	}

	public void add (VisualComponentGroup group) {
		childGroups.add(group);
		group.parent = this;
	}

	public void add (VisualComponent component) {
		components.add(component);
	}

	public void add (VisualConnection connection) {
		connections.add(connection);
	}

	public void remove (VisualComponentGroup group) {
		childGroups.remove(group);
	}

	public void remove (VisualComponent component) {
		components.remove(component);
	}

	public void remove (VisualConnection connection) {
		connections.remove(connections);
	}

	@Override
	public void toXML(Element groupElement) {
		super.toXML(groupElement);

		for (VisualComponent vcomp : components) {
			Element vcompElement = groupElement.getOwnerDocument().createElement("component");
			XmlUtil.writeIntAttr(vcompElement, "ref", vcomp.getReferencedComponent().getID());
			vcomp.toXML(vcompElement);
			groupElement.appendChild(vcompElement);
		}

		for (VisualConnection vcon : connections) {
			Element vconElement = groupElement.getOwnerDocument().createElement("connection");
			XmlUtil.writeIntAttr(vconElement, "ref", vcon.getReferencedConnection().getID());
			vcon.toXML(vconElement);
			groupElement.appendChild(vconElement);
		}

		for (VisualComponentGroup group : childGroups) {
			Element childGroupElement = groupElement.getOwnerDocument().createElement("group");
			group.toXML(childGroupElement);
			groupElement.appendChild(childGroupElement);
		}
	}

	public LinkedList<Selectable> hitObjects(Rectangle2D rectInLocalSpace) {
		LinkedList<Selectable> hit = new LinkedList<Selectable>();
		for(VisualComponent comp : components)
			if(rectInLocalSpace.contains(comp.getBoundingBoxInParentSpace()))
				hit.add(comp);
		/*for(VisualConnection conn : this.connections)
			if(rectInLocalSpace.intersects(conn.getBoundingBoxInParentSpace()))
				hit.add(conn);*/
		for(VisualComponentGroup grp : childGroups)
			if(rectInLocalSpace.contains(grp.getBoundingBoxInParentSpace()))
				hit.add(grp);
		return hit;
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return hitObject(pointInLocalSpace)!=null;
	}

	public Selectable hitObject(Point2D pointInLocalSpace) {
		for (VisualComponent comp : components)
			if (comp.getBoundingBoxInParentSpace().contains(pointInLocalSpace))
				if (comp.hitTestInParentSpace(pointInLocalSpace))
					return comp;
		/*for (VisualConnection conn : this.connections)
			if (conn.getBoundingBoxInParentSpace().contains(pointInLocalSpace))
				if (conn.hitTestInParentSpace(pointInLocalSpace))
					return conn;*/
		for(VisualComponentGroup grp : childGroups)
			if(grp.getBoundingBoxInParentSpace().contains(pointInLocalSpace))
				if (grp.hitTestInParentSpace(pointInLocalSpace))
					return grp;
		return null;
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D.Double rect = null;
		for(VisualComponent comp : components)
			if(rect==null) {
				rect = new Rectangle2D.Double();
				rect.setRect(comp.getBoundingBoxInParentSpace());
			}
			else
				rect.add(comp.getBoundingBoxInParentSpace());
		for(VisualComponentGroup grp : childGroups)
			if(rect==null) {
				rect = new Rectangle2D.Double();
				rect.setRect(grp.getBoundingBoxInParentSpace());
			}
			else
				rect.add(grp.getBoundingBoxInParentSpace());
		return rect;
	}

	@Override
	public void clearColorisation() {
		for (VisualComponentGroup g : childGroups)
			g.clearColorisation();
		for (VisualComponent c: components)
			c.clearColorisation();
		for (VisualConnection c: connections)
			c.clearColorisation();
	}
}