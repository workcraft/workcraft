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

package org.workcraft.plugins.dfs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.plugins.dfs.decorations.BinaryRegisterDecoration;

@Hotkey(KeyEvent.VK_O)
@DisplayName ("Pop register")
@SVGIcon("images/icons/svg/dfs-pop_register.svg")
public class VisualPopRegister extends VisualBinaryRegister {

    public VisualPopRegister(PopRegister register) {
        super(register);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        double w = size - strokeWidth;
        double h = size - strokeWidth;
        double w2 = w/2;
        double h2 = h/2;
        double dx = size / 5;
        double dy = strokeWidth / 2;
        double dt = (size - strokeWidth) / 8;
        float strokeWidth1 = (float)strokeWidth;
        float strokeWidth2 = strokeWidth1 / 2;

        Shape shape = new Rectangle2D.Double(-w2, -h2, w, h);

        Path2D trueInnerShape = new Path2D.Double();
        trueInnerShape.moveTo(-w2 + dx, +h2 - dy);
        trueInnerShape.lineTo(-w2 + dx, -h2 + dy);
        trueInnerShape.moveTo(+w2 - dx, +h2 - dy);
        trueInnerShape.lineTo(+w2 - dx, -h2 + dy);

        Path2D falseInnerShape = new Path2D.Double();
        falseInnerShape.moveTo(+w2 - dx, +h2 - dy);
        falseInnerShape.lineTo(0, +h2 - 2 * dt);
        falseInnerShape.lineTo(-w2 + dx, +h2 - dy);

        Shape tokenShape = new Ellipse2D.Double(-dt, -dt, 2 * dt, 2 * dt);

        Color defaultColor = Coloriser.colorise(getForegroundColor(), d.getColorisation());
        Color tokenColor = Coloriser.colorise(getTokenColor(), d.getColorisation());
        boolean trueMarked = getReferencedPopRegister().isTrueMarked();
        boolean trueExcited = false;
        boolean falseMarked = getReferencedPopRegister().isFalseMarked();
        boolean falseExcited = false;
        if (d instanceof BinaryRegisterDecoration) {
            defaultColor = getForegroundColor();
            tokenColor = ((BinaryRegisterDecoration)d).getTokenColor();
            trueMarked = ((BinaryRegisterDecoration)d).isTrueMarked();
            trueExcited = ((BinaryRegisterDecoration)d).isTrueExcited();
            falseMarked = ((BinaryRegisterDecoration)d).isFalseMarked();
            falseExcited = ((BinaryRegisterDecoration)d).isFalseExcited();
        }

        g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);

        g.setStroke(new BasicStroke(strokeWidth2));
        if (falseExcited) {
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.draw(falseInnerShape);
        if (trueExcited) {
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.draw(trueInnerShape);

        if (trueExcited || falseExcited) {
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        } else {
            g.setColor(defaultColor);
        }
        g.setStroke(new BasicStroke(strokeWidth1));
        g.draw(shape);

        g.setColor(tokenColor);
        g.setStroke(new BasicStroke(strokeWidth2));
        if (trueMarked) {
            g.fill(tokenShape);
        }
        if (falseMarked) {
            g.draw(tokenShape);
        }

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    public PopRegister getReferencedPopRegister() {
        return (PopRegister)getReferencedComponent();
    }

}
