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

package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.util.ColorGenerator;

@Hotkey(KeyEvent.VK_T)
@DisplayName ("Transition")
@SVGIcon("images/petri-node-transition.svg")
public class VisualTransition extends VisualComponent {
    private ColorGenerator tokenColorGenerator = null;

    public VisualTransition(Transition transition) {
        this(transition, true, true, true);
    }

    public VisualTransition(Transition transition, boolean hasColorProperties, boolean hasLabelProperties, boolean hasNameProperties) {
        super(transition, hasColorProperties, hasLabelProperties, hasNameProperties);
    }

    public Transition getReferencedTransition() {
        return (Transition) getReferencedComponent();
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        double xy = -size / 2 + strokeWidth / 2;
        double wh = size - strokeWidth;
        Shape shape = new Rectangle2D.Double(xy, xy, wh, wh);
        g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        g.setStroke(new BasicStroke((float) strokeWidth));
        g.draw(shape);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)    {
        return (Math.abs(pointInLocalSpace.getX()) <= 0.5 * size) && (Math.abs(pointInLocalSpace.getY()) <= 0.5 * size);
    }

    public ColorGenerator getTokenColorGenerator() {
        return this.tokenColorGenerator;
    }

    public void setTokenColorGenerator(ColorGenerator value) {
        this.tokenColorGenerator = value;
    }

}
