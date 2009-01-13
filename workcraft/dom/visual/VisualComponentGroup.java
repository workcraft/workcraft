package org.workcraft.dom.visual;

import java.awt.Color;
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


public class VisualComponentGroup extends VisualTransformableNode {
	public static final int HIT_COMPONENT = 1;
	public static final int HIT_CONNECTION = 2;
	public static final int HIT_GROUP = 3;

	protected VisualComponentGroup parent = null;

	protected Set<VisualConnection> connections = new HashSet<VisualConnection>();
	protected Set<VisualComponent> components = new HashSet<VisualComponent>();
	protected Set<VisualComponentGroup> groups = new HashSet<VisualComponentGroup>();
	protected Set<VisualNode> children = new HashSet<VisualNode>();

	public VisualComponentGroup (VisualComponentGroup parent) {
		super(parent);
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
		g.transform(localToParentTransform);

		for (VisualConnection connection : connections) {
			AffineTransform rest2 = g.getTransform();
			connection.draw(g);
			g.setTransform(rest2);
		}

		for (VisualComponentGroup group : groups) {
			AffineTransform rest2 = g.getTransform();
			g.transform(group.localToParentTransform);
			group.draw(g);
			// Restore group transform
			g.setTransform(rest2);
		}

		for (VisualComponent component : components) {
			AffineTransform rest2 = g.getTransform();
			g.transform(component.localToParentTransform);
			component.draw(g);
			// Restore group transform
			g.setTransform(rest2);
		}

		// Restore original transform
		g.setTransform(rest1);
	}

	public void add (VisualComponentGroup group) {
		group.getParent().remove(group);
		group.setParent(this);
		groups.add(group);

	}

	public void add (VisualNode node) {
		if (node.getParent() != null)
			node.getParent().remove(node);

		node.setParent(this);
		children.add(node);

		if (node instanceof VisualComponent)
			components.add((VisualComponent)node);
		else if (node instanceof VisualComponentGroup)
			groups.add((VisualComponentGroup)node);
		else if (node instanceof VisualConnection)
			connections.add((VisualConnection)node);
	}

	protected void remove (VisualNode node) {
		node.setParent(null);
		children.remove(node);

		if (node instanceof VisualComponent)
			components.remove((VisualComponent)node);
		else if (node instanceof VisualComponentGroup)
			groups.remove((VisualComponentGroup)node);
		else if (node instanceof VisualConnection)
			connections.remove((VisualConnection)node);
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

		for (VisualComponentGroup group : groups) {
			Element childGroupElement = groupElement.getOwnerDocument().createElement("group");
			group.toXML(childGroupElement);
			groupElement.appendChild(childGroupElement);
		}
	}

	public LinkedList<VisualNode> hitObjects(Rectangle2D rectInLocalSpace) {
		LinkedList<VisualNode> hit = new LinkedList<VisualNode>();

		for (VisualNode n : children)
			if(rectInLocalSpace.contains(n.getBoundingBoxInParentSpace()))
				hit.add(n);
		return hit;
	}

	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		VisualNode hit = hitNode(pointInLocalSpace);
		if (hitNode(pointInLocalSpace)==null)
			return 0;
		else
			if (hit instanceof VisualComponent)
				return 1;
			else if (hit instanceof VisualConnection)
				return 2;
			else if (hit instanceof VisualComponentGroup)
				return 3;
		return 0;
	}

	public VisualNode hitNode(Point2D pointInLocalSpace) {
		for(VisualNode node: children)
			if(node.getBoundingBoxInParentSpace().contains(pointInLocalSpace))
				if (node.hitTestInParentSpace(pointInLocalSpace)!=0)
					return node;
		return null;
	}

	public VisualComponent hitComponent(Point2D pointInLocalSpace) {
		for(VisualComponent comp: components)
			if(comp.getBoundingBoxInParentSpace().contains(pointInLocalSpace))
				if (comp.hitTestInParentSpace(pointInLocalSpace)!=0)
					return comp;
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
		for(VisualComponentGroup grp : groups)
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
		colorisation = null;
		for (VisualNode node : children)
			node.clearColorisation();
	}

	@Override
	public void setColorisation(Color color) {
		colorisation = color;
		for (VisualNode node : children)
			node.setColorisation(color);
	}
}