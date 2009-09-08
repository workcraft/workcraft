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
import org.workcraft.framework.PropertySupport;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.framework.observation.ObservableState;
import org.workcraft.framework.observation.ObservableStateImpl;
import org.workcraft.framework.observation.PropertyChangedEvent;
import org.workcraft.framework.observation.StateEvent;
import org.workcraft.framework.observation.StateObserver;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;


public abstract class VisualNode implements PropertyEditable, Node, Touchable, Colorisable, ObservableState {
	ObservableStateImpl observableStateImpl = new ObservableStateImpl();

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

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public void addPropertyDeclaration(PropertyDescriptor declaration) {
		propertySupport.addPropertyDeclaration(declaration);
	}

	public void firePropertyChanged(String propertyName) {
		propertySupport.firePropertyChanged(propertyName, this);
	}

	public Collection<PropertyDescriptor> getPropertyDeclarations() {
		return propertySupport.getPropertyDeclarations();
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
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