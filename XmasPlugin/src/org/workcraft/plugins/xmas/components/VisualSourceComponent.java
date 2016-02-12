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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.SourceComponent.Mode;
import org.workcraft.plugins.xmas.components.SourceComponent.Type;

@DisplayName("Source")
@Hotkey(KeyEvent.VK_I)
@SVGIcon("images/icons/svg/xmas-source.svg")
public class VisualSourceComponent extends VisualXmasComponent {
    public static final String PROPERTY_FOREGROUND_COLOR = "Foreground color";

    public Color color = new Color(0, 255, 0, 255);
    private VisualXmasContact oContact = null;
    public final double tokenSize = 0.18 * size;

    public VisualSourceComponent(SourceComponent component) {
        super(component);
        if (component.getChildren().isEmpty()) {
            oContact = addOutput("o", Positioning.CENTER);
        }
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualSourceComponent, Type>(
                this, SourceComponent.PROPERTY_TYPE, Type.class, true, true, true) {
            protected void setter(VisualSourceComponent object, Type value) {
                object.getReferencedSourceComponent().setType(value);
            }
            protected Type getter(VisualSourceComponent object) {
                return object.getReferencedSourceComponent().getType();
            }
        });
        addPropertyDeclaration(new PropertyDeclaration<VisualSourceComponent, Mode>(
                this, SourceComponent.PROPERTY_MODE, Mode.class, true, true, true) {
            protected void setter(VisualSourceComponent object, Mode value) {
                object.getReferencedSourceComponent().setMode(value);
            }
            protected Mode getter(VisualSourceComponent object) {
                return object.getReferencedSourceComponent().getMode();
            }
        });
    }

    public SourceComponent getReferencedSourceComponent() {
        return (SourceComponent) getReferencedComponent();
    }

    public VisualXmasContact getOContact() {
        return oContact;
    }

    @Override
    public Shape getShape() {
        Path2D shape = new Path2D.Double();

        shape.moveTo(0.00, 0.00);
        shape.lineTo(0.00, -0.60 * size);

        shape.moveTo(-0.40 * size, -0.60 * size);
        shape.lineTo(+0.40 * size, -0.60 * size);

        return shape;
    }

    public Shape getTokenShape() {
        return new Ellipse2D.Double(-1.8 * tokenSize, -2.5 * tokenSize, tokenSize, tokenSize);
    }

    @Override
    public void draw(DrawRequest r) {
        super.draw(r);
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        if (d instanceof StateDecoration) {
            if (((StateDecoration) d).getState()) {
                g.setStroke(new BasicStroke((float) XmasSettings.getBorderWidth()));
                g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
                Shape shape = transformShape(getTokenShape());
                g.fill(shape);
                g.draw(shape);
            }
        }
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualSourceComponent) {
            SourceComponent srcComponent = ((VisualSourceComponent) src).getReferencedSourceComponent();
            getReferencedSourceComponent().setType(srcComponent.getType());
            getReferencedSourceComponent().setMode(srcComponent.getMode());
        }
    }

}
