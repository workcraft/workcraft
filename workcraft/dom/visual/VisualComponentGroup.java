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
import org.workcraft.dom.MathModel;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.util.XmlUtil;


public class VisualComponentGroup extends VisualNode {
	protected VisualComponentGroup parent = null;

	protected Set<VisualComponentGroup> childGroups;
	protected Set<VisualComponent> components;
	protected Set<VisualConnection> connections;

	public VisualComponentGroup () {
		super();
		childGroups = new HashSet<VisualComponentGroup>();
		components = new HashSet<VisualComponent>();
		connections = new HashSet<VisualConnection>();
	}

	public VisualComponentGroup (Element element, MathModel refModel) throws VisualModelConstructionException {
		this();

		NodeList nodes = element.getElementsByTagName("component");

		for (int i=0; i<nodes.getLength(); i++) {
			Element vcompElement = (Element)nodes.item(i);
			int ref = XmlUtil.readIntAttr(vcompElement, "ref", -1);
			Component refComponent = refModel.getComponentByID(ref);
			if (refComponent == null)
				throw new VisualModelConstructionException ("a visual component references to the model component with ID=" +
						vcompElement.getAttribute("ref")+", which was not found");
			VisualComponent visualComponent = (VisualComponent)PluginManager.createVisualClassFor(refComponent, VisualComponent.class, vcompElement);
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
			VisualConnection visualConnection = (VisualConnection)PluginManager.createVisualClassFor(refConnection, VisualConnection.class, vconElement);
			if (visualConnection == null)
				throw new VisualModelConstructionException ("a visual component references to the model component with ID=" +
						vconElement.getAttribute("ref")+", which does not declare a visual represenation class");
			add(visualConnection);
		}

		nodes = element.getElementsByTagName("group");

		for (int i=0; i<nodes.getLength(); i++) {
			Element groupElement = (Element)nodes.item(i);
			add (new VisualComponentGroup (groupElement, refModel));
		}
	}



	public void draw(Graphics2D g) {
		AffineTransform rest1 = g.getTransform();

		// Apply group transform
		g.transform(transform);

		for (VisualConnection connection : connections) {
			connection.draw();
		}

		for (VisualComponentGroup group : childGroups) {
			AffineTransform rest2 = g.getTransform();
			group.draw(g);
			// Restore group transform
			g.setTransform(rest2);
		}

		for (VisualComponent component : components) {
			AffineTransform rest2 = g.getTransform();
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

	public void toXML(Element groupElement) {
		super.toXML(groupElement);

		for (VisualComponent vcomp : components) {
			Element vcompElement = groupElement.getOwnerDocument().createElement("component");
			XmlUtil.writeIntAttr(vcompElement, "ref", vcomp.getReferencedComponent().getID());
			vcomp.toXML(vcompElement);
		}

		for (VisualConnection vcon : connections) {
			Element vconElement = groupElement.getOwnerDocument().createElement("connection");
			XmlUtil.writeIntAttr(vconElement, "ref", vcon.getReferencedConnection().getID());
			vcon.toXML(vconElement);
		}

		for (VisualComponentGroup group : childGroups) {
			Element childGroupElement = groupElement.getOwnerDocument().createElement("group");
			group.toXML(childGroupElement);
		}
	}

	public Selectable hitObject(Point2D point) {
		Selectable hit = null;
		for(VisualComponent comp : components) {
			if(comp.hitTest(point))
				hit = comp;
		}
		for(VisualComponentGroup grp : childGroups) {
			if(grp.hitTest(point))
				hit = grp;
		}
		for(VisualConnection conn : connections) {
			if(conn.hitTest(point))
				hit = conn;
		}
		return hit; // have to return the last encountered hit since it is the uppermost in z-order
	}

	public LinkedList<Selectable> hitObjects(Rectangle2D rect) {
		LinkedList<Selectable> hit = new LinkedList<Selectable>();
		for(VisualComponent comp : components) {
			if(rect.contains(comp.getBoundingBox()))
				hit.add(comp);
		}
		for(VisualConnection conn : connections) {
			if(rect.contains(conn.getBoundingBox()))
				hit.add(conn);
		}
		for(VisualComponentGroup grp : childGroups) {
			if(rect.contains(grp.getBoundingBox()))
				hit.add(grp);
		}
		return hit;
	}


	public boolean hitTest(Point2D point) {
		return hitObject(point)!=null;
	}


	public Rectangle2D getBoundingBox() {
		Rectangle2D.Double rect = null;
		for(VisualComponent comp : components) {
			if(rect==null) {
				rect = new Rectangle2D.Double();
				rect.setRect(comp.getBoundingBox());
			}
			else
				rect.add(comp.getBoundingBox());
		}
		for(VisualConnection conn : connections) {
			if(rect==null) {
				rect = new Rectangle2D.Double();
				rect.setRect(conn.getBoundingBox());
			}
			else
				rect.add(conn.getBoundingBox());
		}
		for(VisualComponentGroup grp : childGroups) {
			if(rect==null) {
				rect = new Rectangle2D.Double();
				rect.setRect(grp.getBoundingBox());
			}
			else
				rect.add(grp.getBoundingBox());
		}
		return rect;
	}
}