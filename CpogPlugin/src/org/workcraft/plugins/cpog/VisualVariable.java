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

package org.workcraft.plugins.cpog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_X)
@DisplayName("Variable")
@SVGIcon("images/icons/svg/variable.svg")
public class VisualVariable extends VisualComponent {
    public static final String PROPERTY_LABEL = "Label";
    private static Font variableFont;
    private static Font valueFont;

    private final RenderedFormula valueFalseRenderedFormula = new RenderedFormula(
            VariableState.FALSE.getValueAsString(), One.instance(),
            valueFont, Positioning.CENTER, new Point2D.Double(0.0, 0.0));

    private final RenderedFormula valueTrueRenderedFormula = new RenderedFormula(
            VariableState.TRUE.getValueAsString(), One.instance(),
            valueFont, Positioning.CENTER, new Point2D.Double(0.0, 0.0));

    private final RenderedFormula valueUndefinedRenderedFormula = new RenderedFormula(
            VariableState.UNDEFINED.getValueAsString(), One.instance(),
            valueFont, Positioning.CENTER, new Point2D.Double(0.0, 0.0));

    private RenderedFormula variableRenderedFormula = new RenderedFormula("", One.instance(), variableFont, getLabelPositioning(), getLabelOffset());

    static {
        try {
            Font font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/default.pfb"));
            variableFont = font.deriveFont(0.5f);
            valueFont = font.deriveFont(0.75f);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VisualVariable(Variable variable) {
        super(variable);
        addPropertyDeclaration(new PropertyDeclaration<VisualVariable, VariableState>(
                this, Variable.PROPERTY_STATE, VariableState.class, true, true, true) {
            public void setter(VisualVariable object, VariableState value) {
                object.setState(value);
            }
            public VariableState getter(VisualVariable object) {
                return object.getState();
            }
        });
        removePropertyDeclarationByName("Name positioning");
        removePropertyDeclarationByName("Name color");
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        Color background = r.getDecoration().getBackground();

        Shape shape = new Rectangle2D.Double(-size / 2 + strokeWidth / 2, -size / 2 + strokeWidth / 2,
                size - strokeWidth, size - strokeWidth);

        g.setStroke(new BasicStroke((float) strokeWidth));
        g.setColor(Coloriser.colorise(getFillColor(), background));
        g.fill(shape);
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        g.draw(shape);

        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        switch (getState()) {
        case FALSE:
            valueFalseRenderedFormula.draw(g);
            break;
        case TRUE:
            valueTrueRenderedFormula.draw(g);
            break;
        case UNDEFINED:
            valueUndefinedRenderedFormula.draw(g);
            break;
        default:
            break;
        }
        drawVariableInLocalSpace(r);
    }

    protected void cacheVariableRenderedFormula(DrawRequest r) {
        if (variableRenderedFormula.isDifferent(getLabel(), One.instance(), variableFont, getLabelPositioning(), getLabelOffset())) {
            variableRenderedFormula = new RenderedFormula(getLabel(), One.instance(), variableFont, getLabelPositioning(), getLabelOffset());
        }
    }

    protected void drawVariableInLocalSpace(DrawRequest r) {
        if (getLabelVisibility()) {
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            cacheVariableRenderedFormula(r);
            if ((variableRenderedFormula != null) && !variableRenderedFormula.isEmpty()) {
                g.setColor(Coloriser.colorise(getLabelColor(), d.getColorisation()));
                variableRenderedFormula.draw(g);
            }
        }
    }

    @NoAutoSerialisation
    @Override
    public String getLabel() {
        return getMathVariable().getLabel();
    }

    @NoAutoSerialisation
    @Override
    public void setLabel(String label)    {
        getMathVariable().setLabel(label);
        sendNotification(new PropertyChangedEvent(this, PROPERTY_LABEL));
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        if (getLabelVisibility() && (variableRenderedFormula != null) && !variableRenderedFormula.isEmpty()) {
            bb = BoundingBoxHelper.union(bb, variableRenderedFormula.getBoundingBox());
        }
        return bb;
    }

    public Variable getMathVariable() {
        return (Variable) getReferencedComponent();
    }

    public VariableState getState() {
        return getMathVariable().getState();
    }

    public void setState(VariableState state) {
        getMathVariable().setState(state);
    }

    public void toggle() {
        setState(getState().toggle());
    }

}
