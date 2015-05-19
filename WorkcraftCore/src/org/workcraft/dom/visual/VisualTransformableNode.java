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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.w3c.dom.Element;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;


public abstract class VisualTransformableNode extends VisualNode implements Movable, Rotatable, Flippable {
	protected AffineTransform localToParentTransform = new AffineTransform();
	protected AffineTransform parentToLocalTransform = new AffineTransform();

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualTransformableNode, Double>(
				this, "X", Double.class, true, true, false) {
			@Override
			public void setter(VisualTransformableNode object, Double value) {
				object.setRootSpaceX(value);
			}
			@Override
			public Double getter(VisualTransformableNode object) {
				return object.getRootSpaceX();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualTransformableNode, Double>(
				this, "Y", Double.class, true, true, false) {
			@Override
			public void setter(VisualTransformableNode object, Double value) {
				object.setRootSpaceY(value);
			}
			@Override
			public Double getter(VisualTransformableNode object) {
				return object.getRootSpaceY();
			}
		});
	}

	public VisualTransformableNode() {
		super();
		addPropertyDeclarations();
	}

	public VisualTransformableNode (Element visualNodeElement) {
		super();
		addPropertyDeclarations();
		VisualTransformableNodeDeserialiser.initTransformableNode(visualNodeElement, this);
	}

	@NoAutoSerialisation
	public double getX() {
		return getLocalToParentTransform().getTranslateX();
	}

	@NoAutoSerialisation
	public void setX(double value) {
		transformChanging();
		double dx = value - getLocalToParentTransform().getTranslateX();
		localToParentTransform.translate(dx, 0);
		transformChanged();
	}

	@NoAutoSerialisation
	private double getRootSpaceX() {
		double result = 0.0;
		Node node = this;
		while (node != null) {
			if (node instanceof VisualTransformableNode) {
				result += ((VisualTransformableNode)node).getX();
			}
			node = node.getParent();
		}
		return result;
	}

	@NoAutoSerialisation
	private void setRootSpaceX(double value) {
		Node node = getParent();
		while (node != null) {
			if (node instanceof VisualTransformableNode) {
				value -= ((VisualTransformableNode)node).getX();
			}
			node = node.getParent();
		}
		setX(value);
	}

	@NoAutoSerialisation
	public double getY() {
		return getLocalToParentTransform().getTranslateY();
	}

	@NoAutoSerialisation
	public void setY(double value) {
		transformChanging();
		double dy = value - getLocalToParentTransform().getTranslateY();
		localToParentTransform.translate(0, dy);
		transformChanged();
	}

	@NoAutoSerialisation
	private double getRootSpaceY() {
		double result = 0.0;
		Node node = this;
		while (node != null) {
			if (node instanceof VisualTransformableNode) {
				result += ((VisualTransformableNode)node).getY();
			}
			node = node.getParent();
		}
		return result;
	}

	@NoAutoSerialisation
	private void setRootSpaceY(double value) {
		Node node = getParent();
		while (node != null) {
			if (node instanceof VisualTransformableNode) {
				value -= ((VisualTransformableNode)node).getY();
			}
			node = node.getParent();
		}
		setY(value);
	}

	@NoAutoSerialisation
	public void setRootSpacePosition(Point2D pos) {
		setRootSpaceX(pos.getX());
		setRootSpaceY(pos.getY());
	}

	@NoAutoSerialisation
	public Point2D getRootSpacePosition() {
		return new Point2D.Double(getRootSpaceX(), getRootSpaceY());
	}

	@NoAutoSerialisation
	public void setPosition(Point2D pos) {
		transformChanging();
		double dx = pos.getX() - getLocalToParentTransform().getTranslateX();
		double dy = pos.getY() - getLocalToParentTransform().getTranslateY();
		localToParentTransform.setToTranslation(pos.getX(), pos.getY());
//		localToParentTransform.translate(dx, dy);
		transformChanged();
	}

	@NoAutoSerialisation
	public Point2D getPosition() {
		return new Point2D.Double(getX(), getY());
	}

	protected void transformChanging() {
		sendNotification(new TransformChangingEvent(this));
	}

	protected void transformChanged() {
		parentToLocalTransform = Geometry.optimisticInverse(getLocalToParentTransform());
		sendNotification(new TransformChangedEvent(this));
	}

	public abstract boolean hitTestInLocalSpace(Point2D pointInLocalSpace);

	@Override
	public boolean hitTest(Point2D point) {
		return hitTestInLocalSpace(getParentToLocalTransform().transform(point, null));
	}

	public abstract Rectangle2D getBoundingBoxInLocalSpace();

	@Override
    public final Rectangle2D getBoundingBox() {
    	return transformToParentSpace(getBoundingBoxInLocalSpace());
    }

    public final Rectangle2D getBoundingBoxInRootSpace() {
		return BoundingBoxHelper.move(getBoundingBox(), getRootSpacePosition());
    }

	public abstract Point2D getCenterInLocalSpace();

	@Override
    public final Point2D getCenter() {
    	return getLocalToParentTransform().transform(getCenterInLocalSpace(), null);
    }

	protected Rectangle2D transformToParentSpace(Rectangle2D rect) {
		if(rect == null)
    		return null;

		Point2D p0 = new Point2D.Double(rect.getMinX(), rect.getMinY());
		Point2D p1 = new Point2D.Double(rect.getMaxX(), rect.getMaxY());

		AffineTransform t = getLocalToParentTransform();
		t.transform(p0, p0);
		t.transform(p1, p1);

		Rectangle2D.Double result = new Rectangle2D.Double(p0.getX(), p0.getY(), 0, 0);
		result.add(p1);

		return result;
	}

	public AffineTransform getLocalToParentTransform() {
		return localToParentTransform;
	}

	public AffineTransform getParentToLocalTransform() {
		return parentToLocalTransform;
	}

	@Override
	public void applyTransform(AffineTransform transform) {
		transformChanging();
		localToParentTransform.preConcatenate(transform);
		transformChanged();
	}

	@NoAutoSerialisation
	public double getRotation() {
		return 0;
	}

	@NoAutoSerialisation
	public double getScaleX() {
		return 0;
	}

	@NoAutoSerialisation
	public double getScaleY() {
		return 0;
	}

	@Override
	public AffineTransform getTransform() {
		return getLocalToParentTransform();
	}

	@NoAutoSerialisation
	public void setRotation(double rotation) {

	}

	@NoAutoSerialisation
	public void setScaleX(double scaleX) {

	}

	@NoAutoSerialisation
	public void setScaleY(double scaleY) {

	}

	public void setTransform(AffineTransform transform) {
		transformChanging();
		localToParentTransform.setTransform(transform);
		transformChanged();
	}

	@Override
	public void copyPosition(Movable src) {
		if (src instanceof VisualTransformableNode) {
			VisualTransformableNode srcNode = (VisualTransformableNode)src;
			setPosition(srcNode.getPosition());
		}
	}

	@Override
	public void rotateClockwise() {
		for (Node node: getChildren()) {
			if (node instanceof Rotatable) {
				((Rotatable)node).rotateClockwise();
			}
		}
	}

	@Override
	public void rotateCounterclockwise() {
		for (Node node: getChildren()) {
			if (node instanceof Rotatable) {
				((Rotatable)node).rotateCounterclockwise();
			}
		}
	}

	@Override
	public void flipHorizontal() {
		for (Node node: getChildren()) {
			if (node instanceof Flippable) {
				((Flippable)node).flipHorizontal();
			}
		}
	}

	@Override
	public void flipVertical() {
		for (Node node: getChildren()) {
			if (node instanceof Flippable) {
				((Flippable)node).flipVertical();
			}
		}
	}

	public Collection<VisualComponent> getComponents() {
		 return Hierarchy.getChildrenOfType(this, VisualComponent.class);
	}

	public Collection<VisualConnection> getConnections() {
		return Hierarchy.getChildrenOfType(this, VisualConnection.class);
	}

	public String getLabel() {
		return "no label";
	}

}
