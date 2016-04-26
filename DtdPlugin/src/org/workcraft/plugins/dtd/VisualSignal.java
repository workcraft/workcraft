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

package org.workcraft.plugins.dtd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;

@Hotkey(KeyEvent.VK_X)
@DisplayName("Signal")
@SVGIcon("images/icons/svg/rho.svg")
public class VisualSignal extends VisualComponent {

    public VisualSignal(Signal signal) {
        super(signal);
        removePropertyDeclarationByName("Name positioning");
        removePropertyDeclarationByName("Name color");
        removePropertyDeclarationByName("Label");
        removePropertyDeclarationByName("Label positioning");
        removePropertyDeclarationByName("Label color");
    }

    public Shape getShape() {
        Path2D shape = new Path2D.Double();
        double h = 0.2 * size;
        double w2 = 0.05 * size;
        // One
        shape.moveTo(-w2, -0.5 * size + w2);
        shape.lineTo(0.0, -0.5 * size);
        shape.lineTo(0.0, -0.5 * size + h);
        shape.moveTo(-w2, -0.5 * size + h);
        shape.lineTo(+w2, -0.5 * size + h);
        // Zero
        shape.moveTo(-w2, 0.5 * size);
        shape.lineTo(+w2, 0.5 * size);
        shape.lineTo(+w2, 0.5 * size - h);
        shape.lineTo(-w2, 0.5 * size - h);
        shape.closePath();
        return shape;
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        Shape shape = getShape();
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        g.setStroke(new BasicStroke(0.1f * (float) strokeWidth));
        g.draw(shape);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return BoundingBoxHelper.expand(getShape().getBounds2D(), 0.5 * size, 0.0);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
    }

    @Override
    public Positioning getNamePositioning() {
        return Positioning.LEFT;
    }

    public Signal getReferencedSignal() {
        return (Signal) getReferencedComponent();
    }

    @Override
    public boolean getLabelVisibility() {
        return false;
    }

}
