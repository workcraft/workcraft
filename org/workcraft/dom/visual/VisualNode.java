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

package org.workcraft.dom.visual;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JPopupMenu;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.PopupMenuBuilder.PopupMenuSegment;
import org.workcraft.exceptions.NotAnAncestorException;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.gui.propertyeditor.PropertySupport;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;


public abstract class VisualNode implements Properties, Node, Touchable, Colorisable, ObservableState, Hidable {
	protected ObservableStateImpl observableStateImpl = new ObservableStateImpl();

	public Rectangle2D getBoundingBox() {
		return null;
	}

	public Collection<Node> getChildren() {
		return Collections.emptyList();
	}

	private Color colorisation = null;
	private Node parent = null;
	private boolean hidden = false;

	private PopupMenuBuilder popupMenuBuilder = new PopupMenuBuilder();
	private PropertySupport propertySupport = new PropertySupport();

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}


	public final AffineTransform getAncestorToParentTransform(VisualGroup ancestor) throws NotAnAncestorException {
		return Geometry.optimisticInverse(getParentToAncestorTransform(ancestor));
	}

	public final AffineTransform getParentToAncestorTransform(VisualGroup ancestor) throws NotAnAncestorException{
		AffineTransform transform = TransformHelper.getTransformToAncestor(this, ancestor);
		transform.preConcatenate(ancestor.parentToLocalTransform);
		return transform;
	}

	public final Rectangle2D getBoundingBoxInAncestorSpace(VisualGroup ancestor) throws NotAnAncestorException {
		Rectangle2D parentBB = getBoundingBox();

		Point2D p0 = new Point2D.Double(parentBB.getMinX(), parentBB.getMinY());
		Point2D p1 = new Point2D.Double(parentBB.getMaxX(), parentBB.getMaxY());

		AffineTransform t = getParentToAncestorTransform(ancestor);
		t.transform(p0, p0);
		t.transform(p1, p1);

		return new Rectangle2D.Double (
				p0.getX(), p0.getY(),
				p1.getX()-p0.getX(),p1.getY() - p0.getY()
		);
	}

	public void setColorisation (Color color) {
		colorisation = color;
	}

	public Color getColorisation () {
		return colorisation;
	}

	public void clearColorisation() {
		setColorisation(null);
	}

	public final boolean isDescendantOf(VisualGroup group) {
		return Hierarchy.isDescendant(this, group);
	}
	protected final void addPopupMenuSegment (PopupMenuSegment segment) {
		popupMenuBuilder.addSegment(segment);
	}

	public final JPopupMenu createPopupMenu(ScriptedActionListener actionListener) {
		return popupMenuBuilder.build(actionListener);
	}

	public void addPropertyDeclaration(PropertyDescriptor declaration) {
		propertySupport.addPropertyDeclaration(declaration);
	}

	public Collection<PropertyDescriptor> getDescriptors() {
		return propertySupport.getPropertyDeclarations();
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;

		sendNotification(new PropertyChangedEvent(this, "hidden"));
	}

	public void addObserver(StateObserver obs) {
		observableStateImpl.addObserver(obs);
	}

	public void sendNotification(StateEvent e) {
		observableStateImpl.sendNotification(e);
	}

	public void removeObserver(StateObserver obs) {
		observableStateImpl.removeObserver(obs);
	}
}