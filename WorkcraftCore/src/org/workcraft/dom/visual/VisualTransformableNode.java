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

import org.w3c.dom.Element;
import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Geometry;


public abstract class VisualTransformableNode extends VisualNode implements Movable {
	protected AffineTransform localToParentTransform = new AffineTransform();
	protected AffineTransform parentToLocalTransform = new AffineTransform();

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualTransformableNode, Double>(
				this, "X", Double.class) {
			public void setter(VisualTransformableNode object, Double value) {
				Node node = object.getParent();
				while ((node != null) && (node instanceof VisualTransformableNode)) {
					VisualTransformableNode container = (VisualTransformableNode)node;
					value -= container.getX();
					node = container.getParent();
				}
				object.setX(value);
			}
			public Double getter(VisualTransformableNode object) {
				double result = 0.0;
				Node node = object;
				while ((node != null) && (node instanceof VisualTransformableNode)) {
					VisualTransformableNode container = (VisualTransformableNode)node;
					result += container.getX();
					node = container.getParent();
				}
				return result;
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualTransformableNode, Double>(
				this, "Y", Double.class) {
			public void setter(VisualTransformableNode object, Double value) {
				Node node = object.getParent();
				while ((node != null) && (node instanceof VisualTransformableNode)) {
					VisualTransformableNode container = (VisualTransformableNode)node;
					value -= container.getY();
					node = container.getParent();
				}
				object.setY(value);
			}
			public Double getter(VisualTransformableNode object) {
				double result = 0.0;
				Node node = object;
				while ((node != null) && (node instanceof VisualTransformableNode)) {
					VisualTransformableNode container = (VisualTransformableNode)node;
					result += container.getY();
					node = container.getParent();
				}
				return result;
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
		return localToParentTransform.getTranslateX();
	}

	@NoAutoSerialisation
	public double getY() {
		return localToParentTransform.getTranslateY();
	}

	@NoAutoSerialisation
	public void setX(double x, boolean notify) {
		transformChanging(notify);
		localToParentTransform.translate(x-localToParentTransform.getTranslateX(), 0);
		transformChanged(notify);
	}

	@NoAutoSerialisation
	public void setX(double x) {
		setX(x, true);
	}

	@NoAutoSerialisation
	public void setY(double y, boolean notify) {
		transformChanging(notify);
		localToParentTransform.translate(0, y - localToParentTransform.getTranslateY());
		transformChanged(notify);
	}

	@NoAutoSerialisation
	public void setY(double y) {
		setY(y, true);
	}

	@NoAutoSerialisation
	public void setPosition(Point2D pos) {
		transformChanging(true);
		localToParentTransform.translate(pos.getX()-localToParentTransform.getTranslateX(), pos.getY() - localToParentTransform.getTranslateY());
		transformChanged(true);
	}

	@NoAutoSerialisation
	public Point2D getPosition() {
		return new Point2D.Double(getX(), getY());
	}

	protected void transformChanged(boolean notify) {
		parentToLocalTransform = Geometry.optimisticInverse(localToParentTransform);
		if (notify) {
			sendNotification(new TransformChangedEvent(this));
		}
	}

	protected void transformChanging(boolean notify) {
		if (notify) {
			sendNotification(new TransformChangingEvent(this));
		}
	}

	public abstract boolean hitTestInLocalSpace(Point2D pointInLocalSpace);

	public boolean hitTest(Point2D point) {
		return hitTestInLocalSpace(parentToLocalTransform.transform(point, null));
	}

	public abstract Rectangle2D getBoundingBoxInLocalSpace();

    public final Rectangle2D getBoundingBox() {
    	return transformToParentSpace(getBoundingBoxInLocalSpace());
    }

	public abstract Point2D getCenterInLocalSpace();

    public final Point2D getCenter()
    {
    	return getLocalToParentTransform().transform(getCenterInLocalSpace(), null);
    }

	protected Rectangle2D transformToParentSpace(Rectangle2D rect)
	{
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

	public void applyTransform(AffineTransform transform)
	{
		transformChanging(true);
		localToParentTransform.preConcatenate(transform);
		transformChanged(true);
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
		setTransform(transform, true);
	}

	public void setTransform(AffineTransform transform, boolean notify) {
		transformChanging(notify);
		localToParentTransform.setTransform(transform);
		transformChanged(notify);
	}

}