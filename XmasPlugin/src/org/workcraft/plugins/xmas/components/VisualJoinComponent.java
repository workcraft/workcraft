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

import java.awt.Shape;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Positioning;

@DisplayName("Join")
@SVGIcon("images/icons/svg/xmas-join.svg")
public class VisualJoinComponent extends VisualXmasComponent {

    public VisualJoinComponent(JoinComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            this.addInput("a", Positioning.TOP_LEFT);
            this.addInput("b", Positioning.BOTTOM_LEFT);
            this.addOutput("o", Positioning.RIGHT);
        }
    }

    public JoinComponent getReferencedJoinComponent() {
        return (JoinComponent) getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(-0.50 * size, -0.50 * size);
        shape.lineTo(-0.18 * size, -0.50 * size);

        shape.moveTo(-0.50 * size, +0.50 * size);
        shape.lineTo(-0.18 * size, +0.50 * size);

        shape.moveTo(-0.10 * size, -0.60 * size);
        shape.lineTo(-0.10 * size, +0.60 * size);

        shape.moveTo(+0.10 * size, -0.60 * size);
        shape.lineTo(+0.10 * size, +0.60 * size);

        shape.moveTo(+0.10 * size, +0.00);
        shape.lineTo(+0.50 * size, +0.00);

        // Arrows
        shape.moveTo(-0.25 * size, -0.55 * size);
        shape.lineTo(-0.15 * size, -0.50 * size);
        shape.lineTo(-0.25 * size, -0.45 * size);
        shape.closePath();

        shape.moveTo(-0.25 * size, +0.55 * size);
        shape.lineTo(-0.15 * size, +0.50 * size);
        shape.lineTo(-0.25 * size, +0.45 * size);
        shape.closePath();

        return shape;
    }

}
