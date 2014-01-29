/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.xmas.components;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.XmasContact.IOType;

public abstract class VisualXmasComponent extends VisualComponent implements Container, StateObserver, ObservableHierarchy {

	public enum Orientation {
		ORIENTATION_0("0°", 0),
		ORIENTATION_90("90°", 1),
		ORIENTATION_180("180°", 2),
		ORIENTATION_270("270°", 3);

		private final String name;
		private final int quadrant;

		private Orientation(String name, int quadrant) {
			this.name = name;
			this.quadrant = quadrant;
		}

		static public Map<String, Orientation> getChoice() {
			LinkedHashMap<String, Orientation> choice = new LinkedHashMap<String, Orientation>();
			for (Orientation item : Orientation.values()) {
				choice.put(item.name, item);
			}
			return choice;
		}
	};

	private Orientation orientation = Orientation.ORIENTATION_0;
	protected DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

	public VisualXmasComponent(XmasComponent component) {
		super(component);
		component.addObserver(this);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualXmasComponent, Orientation>(
				this, "Orientation", Orientation.class, Orientation.getChoice()) {
			protected void setter(VisualXmasComponent object, Orientation value) {
				object.setOrientation(value);
			}
			protected Orientation getter(VisualXmasComponent object) {
				return object.getOrientation();
			}
		});
	}

	public XmasComponent getReferencedXmasComponent() {
		return (XmasComponent)getReferencedComponent();
	}

	public VisualXmasComponent.Orientation getOrientation() {
		return orientation;
	}

    public void setOrientation(VisualXmasComponent.Orientation orientation) {
		if (this.orientation != orientation) {
			transformChanging(true);
			localToParentTransform.quadrantRotate(orientation.quadrant - this.orientation.quadrant);
			this.orientation = orientation;
			transformChanged(true);
		}
	}

    public Collection<VisualXmasContact> getContacts() {
    	ArrayList<VisualXmasContact> result = new ArrayList<VisualXmasContact>();
    	for (Node n: getChildren()) {
    		if (n instanceof VisualXmasContact) {
    			result.add((VisualXmasContact)n);
    		}
    	}
    	return result;
    }

	public void addContact(VisualXmasContact vc, Positioning positioning) {
		if (!getChildren().contains(vc)) {
			this.getReferencedXmasComponent().add(vc.getReferencedComponent());
			add(vc);
			vc.setX(size / 2 * positioning.xSign);
			vc.setY(size / 2 * positioning.ySign);
		}
	}
	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D bb = new Rectangle2D.Double(-size/2, -size/2, size, size);
		for (VisualXmasContact c: getContacts()) {
			Rectangle2D.union(bb, c.getBoundingBox(), bb);
		}
		return bb;
	}

	@Override
	public void add(Node node) {
		groupImpl.add(node);
		if (node instanceof VisualXmasContact) {
			((VisualXmasContact)node).addObserver(this);
		}
	}

	@Override
	public Collection<Node> getChildren() {
		return groupImpl.getChildren();
	}

	@Override
	public Node getParent() {
		return groupImpl.getParent();
	}

	@Override
	public void setParent(Node parent) {
		groupImpl.setParent(parent);
	}

	@Override
	public void remove(Node node) {}

	@Override
	public void add(Collection<Node> nodes) {
		for(Node x : nodes){
			add(x);
		}
	}

	@Override
	public void remove(Collection<Node> nodes) {
	}


	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}

	@Override
	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

	@Override
	public void notify(StateEvent e) {
	}

	public VisualXmasContact addInput(String name, Positioning positioning) {
		XmasContact c = new XmasContact(IOType.INPUT);
		VisualXmasContact vc = new VisualXmasContact(c, name);
		addContact(vc, positioning);
		return vc;
	}

	public VisualXmasContact addOutput(String name, Positioning positioning) {
		XmasContact c = new XmasContact(IOType.OUTPUT);
		VisualXmasContact vc = new VisualXmasContact(c, name);
		addContact(vc, positioning);
		return vc;
	}

	@Override
	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
	}

	@Override
	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}

	abstract public Shape getShape();

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		g.setStroke(new BasicStroke((float)XmasSettings.getBorderWidth()));
		g.draw(getShape());

		AffineTransform at = new AffineTransform();
		at.quadrantRotate(-orientation.quadrant);
		r.getGraphics().transform(at);

		drawNameInLocalSpace(r);
		drawLabelInLocalSpace(r);
	}

}
