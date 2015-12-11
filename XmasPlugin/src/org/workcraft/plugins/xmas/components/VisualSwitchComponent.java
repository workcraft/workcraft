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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.SwitchComponent.Type;  //fff
import org.workcraft.plugins.xmas.components.SwitchComponent.Val;


@DisplayName("Switch")
@SVGIcon("images/icons/svg/xmas-switch.svg")
public class VisualSwitchComponent extends VisualXmasComponent {

	public final double pointerSize = 0.20 * size;

	public VisualSwitchComponent(SwitchComponent component) {
		super(component);
		if (component.getChildren().isEmpty()) {
			this.addInput("i", Positioning.LEFT);
			this.addOutput("a", Positioning.TOP_RIGHT);
			this.addOutput("b", Positioning.BOTTOM_RIGHT);
		}
		addPropertyDeclarations();  //fff
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualSwitchComponent, Type>(  //fff
				this, SwitchComponent.PROPERTY_TYPE, Type.class, true, true, true) {
			protected void setter(VisualSwitchComponent object, Type value) {
				object.getReferencedSwitchComponent().setType(value);
			}
			protected Type getter(VisualSwitchComponent object) {
				return object.getReferencedSwitchComponent().getType();
			}
		});
		addPropertyDeclaration(new PropertyDeclaration<VisualSwitchComponent, Val>(  //fff
				this, SwitchComponent.PROPERTY_VAL, Val.class, true, true, true) {
			protected void setter(VisualSwitchComponent object, Val value) {
				object.getReferencedSwitchComponent().setVal(value);
			}
			protected Val getter(VisualSwitchComponent object) {
				return object.getReferencedSwitchComponent().getVal();
			}
		});
	}

	public SwitchComponent getReferencedSwitchComponent() {
		return (SwitchComponent)getReferencedComponent();
	}

	public Shape getUpPointerShape() {
		Path2D shape = new Path2D.Double();
		shape.moveTo(+0.50 * size, -0.28 * size);
		shape.lineTo(+0.50 * size + 0.7 * pointerSize, -0.28 * size + pointerSize);
		shape.lineTo(+0.50 * size - 0.7 * pointerSize, -0.28 * size + pointerSize);
		shape.closePath();
		return shape;
	}

	public Shape getDownPointerShape() {
		Path2D shape = new Path2D.Double();
		shape.moveTo(+0.50 * size, +0.28 * size);
		shape.lineTo(+0.50 * size + 0.7 * pointerSize, +0.28 * size - pointerSize);
		shape.lineTo(+0.50 * size - 0.7 * pointerSize, +0.28 * size - pointerSize);
		shape.closePath();
		return shape;
	}

	@Override
	public void draw(DrawRequest r) {
		super.draw(r);
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		if (d instanceof StateDecoration) {
			g.setStroke(new BasicStroke((float)XmasSettings.getBorderWidth()));
			g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
			if (((StateDecoration)d).getState()) {
				Shape shape = transformShape(getUpPointerShape());
				g.fill(shape);
				g.draw(shape);
			} else {
				Shape shape = transformShape(getDownPointerShape());
				g.fill(shape);
				g.draw(shape);
			}
		}
	}

	@Override
	public Shape getShape() {
		Path2D shape = new Path2D.Double();

		shape.moveTo(-0.50 * size, +0.00);
		shape.lineTo(       -0.08, +0.00);

		shape.moveTo(        0.00, -0.60 * size);
		shape.lineTo(        0.00, +0.60 * size);

		shape.moveTo(        0.00, -0.50 * size);
		shape.lineTo(+0.50 * size, -0.50 * size);

		shape.moveTo(        0.00, +0.50 * size);
		shape.lineTo(+0.50 * size, +0.50 * size);

		// Arrows
		shape.moveTo(-0.15 * size, -0.05 * size);
		shape.lineTo(-0.05 * size, +0.00);
		shape.lineTo(-0.15 * size, +0.05 * size);
		shape.closePath();

		return shape;
	}

}
