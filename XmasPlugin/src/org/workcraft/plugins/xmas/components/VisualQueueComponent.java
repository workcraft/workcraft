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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonSimulationSettings;

@DisplayName("Queue")
@Hotkey(KeyEvent.VK_Q)
@SVGIcon("images/icons/svg/xmas-queue.svg")
public class VisualQueueComponent extends VisualXmasComponent {

	public static final String PROPERTY_FOREGROUND_COLOR = "Foreground color";

	public final double slotWidth = 0.35 * size;
	public final double slotHeight = 1.0 * size;
	public final double contactLength = 0.5 * size - slotWidth;
	public final double tokenSize = 0.5 * slotWidth;
	public final double headSize = 0.15 * size;
	public final double tailSize = 0.15 * size;

	public Color color = new Color(0, 0, 0, 255);

	public VisualQueueComponent(QueueComponent component) {
		super(component);
		addPropertyDeclarations();
		if (component.getChildren().isEmpty()) {
			this.addInput("i", Positioning.LEFT);
			this.addOutput("o", Positioning.RIGHT);
		}
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualQueueComponent, Integer>(
				this, QueueComponent.PROPERTY_CAPACITY, Integer.class, true, true, true) {
			public void setter(VisualQueueComponent object, Integer value) {
				double scale = (double)value / (double) object.getReferencedQueueComponent().getCapacity();
				for (VisualXmasContact contact: getContacts()) {
					double x = scaleContactCoord(contact.getX(), scale);
					double y = scaleContactCoord(contact.getY(), scale);
					contact.setPosition(new Point2D.Double(x, y));
				}
				object.getReferencedQueueComponent().setCapacity(value);
			}

			private double scaleContactCoord(double val, double scale) {
				double result = val;
				if (val > 0.0) {
					result = scale * (val - contactLength) + contactLength;
				} else if (val < 0.0) {
					result = scale * (val + contactLength) - contactLength;
				}
				return result;
			}

			public Integer getter(VisualQueueComponent object) {
				return object.getReferencedQueueComponent().getCapacity();
			}
		});
	}

	public QueueComponent getReferencedQueueComponent() {
		return (QueueComponent)getReferencedComponent();
	}


	private boolean isInitialised() {
		return (getReferencedQueueComponent() != null);
	}

	private double getSlotOffset(int i) {
		int capacity = getReferencedQueueComponent().getCapacity();
		return (slotWidth * (i - 0.5 * (capacity - 1)));
	}

	public Shape getSlotShape(int index) {
		Path2D shape = new Path2D.Double();
		if (isInitialised()) {
			double w2 = 0.5 * slotWidth;
			double h2 = 0.5 * slotHeight;
			double slotOffset = getSlotOffset(index);
			shape.moveTo(slotOffset - w2, -h2);
			shape.lineTo(slotOffset - w2, +h2);
			shape.lineTo(slotOffset + w2, +h2);
			shape.lineTo(slotOffset + w2, -h2);
			shape.closePath();
		}
		return shape;
	}

	public Shape getTokenShape(int index) {
		Path2D shape = new Path2D.Double();
		if (isInitialised()) {
			double slotOffset = getSlotOffset(index);
			shape.append(new Ellipse2D.Double(slotOffset - 0.5 * tokenSize, -0.5 * tokenSize, tokenSize, tokenSize), false);
		}
		return shape;
	}

	public Shape getHeadShape(int index) {
		Path2D shape = new Path2D.Double();
		if (isInitialised()) {
			double slotOffset = getSlotOffset(index);
			double headOffset = -0.5 * slotHeight;
			shape.moveTo(slotOffset - 0.7 * headSize, headOffset);
			shape.lineTo(slotOffset + 0.00, headOffset + headSize);
			shape.lineTo(slotOffset + 0.7 * headSize, headOffset);
			shape.closePath();
		}
		return shape;
	}

	public Shape getTailShape(int index) {
		Path2D shape = new Path2D.Double();
		if (isInitialised()) {
			double slotOffset = getSlotOffset(index);
			double tailOffset = 0.5 * slotHeight;
			shape.moveTo(slotOffset - 0.7 * tailSize, tailOffset);
			shape.lineTo(slotOffset + 0.00, tailOffset - tailSize);
			shape.lineTo(slotOffset + 0.7 * tailSize, tailOffset);
			shape.closePath();
		}
		return shape;
	}

	@Override
	public Shape getShape() {
		Path2D shape = new Path2D.Double();
		QueueComponent ref = getReferencedQueueComponent();
		if (ref != null) {
			int capacity = ref.getCapacity();
			double contactOffset = 0.5 * capacity * slotWidth;

			shape.moveTo(+contactOffset, 0.0);
			shape.lineTo(+contactOffset + contactLength, 0.0);

			shape.moveTo(-contactOffset, 0.0);
			shape.lineTo(-contactOffset - contactLength, 0.0);

			for (int i = 0; i < capacity; i++) {
				shape.append(getSlotShape(i), false);
			}
		}
		return shape;
	}

	@Override
	public void draw(org.workcraft.dom.visual.DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		if (d instanceof QueueDecoration) {
			int capacity = getReferencedQueueComponent().getCapacity();
			// Quiescent elements
			g.setColor(getForegroundColor());
			for (int i = 0; i < capacity; i++) {
				SlotState slot = ((QueueDecoration)d).getSlotState(i);
				Shape slotShape = transformShape(getSlotShape(i));
				g.draw(slotShape);
				if (!slot.isMemExcited && slot.isFull) {
					Shape tokenShape = transformShape(getTokenShape(i));
					g.draw(tokenShape);
					g.fill(tokenShape);
				}
				if (!slot.isHeadExcited && slot.isHead) {
					Shape headShape = transformShape(getHeadShape(i));
					g.draw(headShape);
					g.fill(headShape);
				}
				if (!slot.isTailExcited && slot.isTail) {
					Shape tailShape = transformShape(getTailShape(i));
					g.draw(tailShape);
					g.fill(tailShape);
				}
			}
			// Excited elements
			g.setColor(Coloriser.colorise(getForegroundColor(), CommonSimulationSettings.getEnabledForegroundColor()));
			for (int i = 0; i < capacity; i++) {
				SlotState slot = ((QueueDecoration)d).getSlotState(i);
				if (slot.isMemExcited) {
					Shape tokenShape = transformShape(getTokenShape(i));
					g.draw(tokenShape);
					if (slot.isFull) {
						g.fill(tokenShape);
					}
				}
				if (slot.isHeadExcited) {
					Shape headShape = transformShape(getHeadShape(i));
					g.draw(headShape);
					if (slot.isHead) {
						g.fill(headShape);
					}
				}
				if (slot.isTailExcited) {
					Shape tailShape = transformShape(getTailShape(i));
					g.draw(tailShape);
					if (slot.isTail) {
						g.fill(tailShape);
					}
				}
			}
		} else {
			super.draw(r);
		}
	}

	@Override
	public void copyStyle(Stylable src) {
		super.copyStyle(src);
		if (src instanceof VisualQueueComponent) {
			QueueComponent srcComponent = ((VisualQueueComponent)src).getReferencedQueueComponent();
			getReferencedQueueComponent().setCapacity(srcComponent.getCapacity());
			getReferencedQueueComponent().setInit(srcComponent.getInit());
		}
	}

}
