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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JPopupMenu;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.PopupMenuBuilder.PopupMenuSegment;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.PropertySupport;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.serialisation.xml.NoAutoSerialisation;


public abstract class VisualNode implements Properties, Node, Touchable, ObservableState, Hidable {
	protected ObservableStateImpl observableStateImpl = new ObservableStateImpl();

	public Rectangle2D getBoundingBox() {
		return null;
	}

	@Override
	public Point2D getCenter()
	{
		return new Point2D.Double(getBoundingBox().getCenterX(), getBoundingBox().getCenterY());
	}

	public Collection<Node> getChildren() {
		return Collections.emptyList();
	}

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

	protected final void addPopupMenuSegment (PopupMenuSegment segment) {
		popupMenuBuilder.addSegment(segment);
	}

	public final JPopupMenu createPopupMenu(ScriptedActionListener actionListener) {
		return popupMenuBuilder.build(actionListener);
	}

	public void addPropertyDeclaration(PropertyDescriptor declaration) {
		propertySupport.addPropertyDeclaration(declaration);
	}

	public void removePropertyDeclaration(PropertyDescriptor declaration) {
		propertySupport.removePropertyDeclaration(declaration);
	}

	public Collection<PropertyDescriptor> getDescriptors() {
		return propertySupport.getPropertyDeclarations();
	}

	@NoAutoSerialisation
	public boolean isHidden() {
		return hidden;
	}

	@NoAutoSerialisation
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
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
