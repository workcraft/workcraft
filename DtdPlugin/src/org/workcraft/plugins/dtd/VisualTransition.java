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
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.dtd.Transition.Direction;
import org.workcraft.plugins.graph.VisualVertex;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Transition")
@SVGIcon("images/dtd-node-transition.svg")
public class VisualTransition extends VisualVertex {

    public VisualTransition(Transition transition) {
        super(transition);
        renamePropertyDeclarationByName("Foreground color", "Color");
        removePropertyDeclarationByName("Fill color");
        removePropertyDeclarationByName("Name");
        removePropertyDeclarationByName("Name positioning");
        removePropertyDeclarationByName("Name color");
        removePropertyDeclarationByName("Label");
        removePropertyDeclarationByName("Label positioning");
        removePropertyDeclarationByName("Label color");
        removePropertyDeclarationByName("Symbol");
        removePropertyDeclarationByName("Symbol positioning");
        removePropertyDeclarationByName("Symbol color");
        removePropertyDeclarationByName("Render type");
    }

    public Shape getShape() {
        Path2D shape = new Path2D.Double();
        if (getReferencedTransition() != null) {
            switch (getReferencedTransition().getDirection()) {
            case PLUS:
                shape.moveTo(0.0, 0.5 * size);
                shape.lineTo(0.0, -0.4 * size + 0.5 * strokeWidth);
                shape.moveTo(0.0, -0.5 * size + strokeWidth);
                shape.lineTo(+0.5 * strokeWidth, -0.4 * size + strokeWidth);
                shape.lineTo(-0.5 * strokeWidth, -0.4 * size + strokeWidth);
                shape.closePath();
                break;
            case MINUS:
                shape.moveTo(0.0, -0.5 * size);
                shape.lineTo(0.0, 0.4 * size - 0.5 * strokeWidth);
                shape.moveTo(0.0, 0.5 * size - strokeWidth);
                shape.lineTo(-0.5 * strokeWidth, 0.4 * size - strokeWidth);
                shape.lineTo(+0.5 * strokeWidth, 0.4 * size - strokeWidth);
                shape.closePath();
                break;
            default:
                break;
            }
        }
        return shape;
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        Shape shape = getShape();
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        g.setStroke(new BasicStroke((float) strokeWidth / 2));
        g.draw(shape);
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return getShape().getBounds2D();
    }

    public Transition getReferencedTransition() {
        return (Transition) getReferencedComponent();
    }

    public Signal getSignal() {
        return getReferencedTransition().getSignal();
    }

    public Direction getDirection() {
        return getReferencedTransition().getDirection();
    }

    @Override
    public boolean getLabelVisibility() {
        return false;
    }

    @Override
    public boolean getNameVisibility() {
        return false;
    }

}
