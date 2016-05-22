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

package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.utils.FormulaRenderingResult;
import org.workcraft.formula.utils.FormulaToGraphics;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Hierarchy;

@DisplayName("Input/output port")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/circuit-port.svg")
public class VisualFunctionContact extends VisualContact implements StateObserver {

    private double size = 0.3;
    private static FontRenderContext context = new FontRenderContext(AffineTransform.getScaleInstance(1000.0, 1000.0), true, true);
    private static Font font;
    static {
        try {
            font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
        } catch (FontFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private FormulaRenderingResult renderedSetFormula = null;
    private FormulaRenderingResult renderedResetFormula = null;

    public VisualFunctionContact(FunctionContact contact) {
        super(contact);
    }

    public FunctionContact getReferencedFunctionContact() {
        return (FunctionContact) getReferencedComponent();
    }

    @NoAutoSerialisation
    public BooleanFormula getSetFunction() {
        return getReferencedFunctionContact().getSetFunction();
    }

    @NoAutoSerialisation
    public void setSetFunction(BooleanFormula setFunction) {
        if (getParent() instanceof VisualFunctionComponent) {
            VisualFunctionComponent p = (VisualFunctionComponent) getParent();
            p.invalidateRenderingResult();
        }
        renderedSetFormula = null;
        getReferencedFunctionContact().setSetFunction(setFunction);
    }

    @NoAutoSerialisation
    public BooleanFormula getResetFunction() {
        return getReferencedFunctionContact().getResetFunction();
    }

    @NoAutoSerialisation
    public void setResetFunction(BooleanFormula resetFunction) {
        if (getParent() instanceof VisualFunctionComponent) {
            VisualFunctionComponent p = (VisualFunctionComponent) getParent();
            p.invalidateRenderingResult();
        }
        renderedResetFormula = null;
        getReferencedFunctionContact().setResetFunction(resetFunction);
    }

    public void invalidateRenderedFormula() {
        renderedSetFormula = null;
        renderedResetFormula = null;
    }

    private FormulaRenderingResult getRenderedSetFormula() {
        if (((FunctionContact) getReferencedContact()).getSetFunction() == null) {
            renderedSetFormula = null;
        } else if (renderedSetFormula == null) {
            renderedSetFormula = FormulaToGraphics.render(((FunctionContact) getReferencedContact()).getSetFunction(), context, font);
        }
        return renderedSetFormula;
    }

    private Point2D getSetFormulaOffset() {
        double xOffset = size;
        double yOffset = -size / 2;
        FormulaRenderingResult renderingResult = getRenderedSetFormula();
        if (renderingResult != null) {
            Direction dir = getDirection();
            if (!(getParent() instanceof VisualFunctionComponent)) {
                dir = dir.flip();
            }
            if ((dir == Direction.SOUTH) || (dir == Direction.WEST)) {
                xOffset = -(size + renderingResult.boundingBox.getWidth());
            }
        }
        return new Point2D.Double(xOffset, yOffset);
    }

    private Rectangle2D getSetBoundingBox() {
        Rectangle2D bb = null;
        FormulaRenderingResult setRenderingResult = getRenderedSetFormula();
        if (setRenderingResult != null) {
            bb = BoundingBoxHelper.move(setRenderingResult.boundingBox, getSetFormulaOffset());
            Direction dir = getDirection();
            if (!(getParent() instanceof VisualFunctionComponent)) {
                dir = dir.flip();
            }
            if ((dir == Direction.NORTH) || (dir == Direction.SOUTH)) {
                AffineTransform rotateTransform = new AffineTransform();
                rotateTransform.quadrantRotate(-1);
                bb = BoundingBoxHelper.transform(bb, rotateTransform);
            }
        }
        return bb;
    }

    private FormulaRenderingResult getRenderedResetFormula() {
        if (((FunctionContact) getReferencedContact()).getResetFunction() == null) {
            renderedResetFormula = null;
        } else if (renderedResetFormula == null) {
            renderedResetFormula = FormulaToGraphics.render(((FunctionContact) getReferencedContact()).getResetFunction(), context, font);
        }
        return renderedResetFormula;
    }

    private Point2D getResetFormulaOffset() {
        double xOffset = size;
        double yOffset = size / 2;
        FormulaRenderingResult renderingResult = getRenderedResetFormula();
        if (renderingResult != null) {
            Direction dir = getDirection();
            if (!(getParent() instanceof VisualFunctionComponent)) {
                dir = dir.flip();
            }
            if ((dir == Direction.SOUTH) || (dir == Direction.WEST)) {
                xOffset = -(size + renderingResult.boundingBox.getWidth());
            }
            yOffset = size / 2 + renderingResult.boundingBox.getHeight();
        }
        return new Point2D.Double(xOffset, yOffset);
    }

    private Rectangle2D getResetBoundingBox() {
        Rectangle2D bb = null;
        FormulaRenderingResult renderingResult = getRenderedResetFormula();
        if (renderingResult != null) {
            bb = BoundingBoxHelper.move(renderingResult.boundingBox, getResetFormulaOffset());
            Direction dir = getDirection();
            if (!(getParent() instanceof VisualFunctionComponent)) {
                dir = dir.flip();
            }
            if ((dir == Direction.NORTH) || (dir == Direction.SOUTH)) {
                AffineTransform rotateTransform = new AffineTransform();
                rotateTransform.quadrantRotate(-1);
                bb = BoundingBoxHelper.transform(bb, rotateTransform);
            }
        }
        return bb;
    }

    private void drawArrow(Graphics2D g, int arrowType, double arrX, double arrY) {
        g.setStroke(new BasicStroke((float) 0.02));
        if (arrowType == 1) {
            // arrow down
            Line2D line = new Line2D.Double(arrX, arrY - 0.15, arrX, arrY - 0.375);
            Path2D path = new Path2D.Double();
            path.moveTo(arrX - 0.05, arrY - 0.15);
            path.lineTo(arrX + 0.05, arrY - 0.15);
            path.lineTo(arrX, arrY);
            path.closePath();
            g.fill(path);
            g.draw(line);
        } else if (arrowType == 2) {
            // arrow up
            Line2D line = new Line2D.Double(arrX, arrY, arrX, arrY - 0.225);
            Path2D path = new Path2D.Double();
            path.moveTo(arrX - 0.05, arrY - 0.225);
            path.lineTo(arrX + 0.05, arrY - 0.225);
            path.lineTo(arrX, arrY - 0.375);
            path.closePath();
            g.fill(path);
            g.draw(line);
        }
    }

    private void drawFormula(Graphics2D g, int arrowType, Point2D offset, FormulaRenderingResult renderingResult) {
        if (renderingResult != null) {
            Direction dir = getDirection();
            if (!(getParent() instanceof VisualFunctionComponent)) {
                dir = dir.flip();
            }

            AffineTransform savedTransform = g.getTransform();
            if ((dir == Direction.NORTH) || (dir == Direction.SOUTH)) {
                AffineTransform rotateTransform = new AffineTransform();
                rotateTransform.quadrantRotate(-1);
                g.transform(rotateTransform);
            }

            double dXArrow = -0.15;
            if ((dir == Direction.SOUTH) || (dir == Direction.WEST)) {
                dXArrow = renderingResult.boundingBox.getWidth() + 0.15;
            }

            drawArrow(g, arrowType, offset.getX() + dXArrow, offset.getY());

            g.translate(offset.getX(), offset.getY());
            renderingResult.draw(g);
            g.setTransform(savedTransform);
        }
    }

    @Override
    public void draw(DrawRequest r) {
        if (needsFormulas()) {
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
            FormulaRenderingResult renderingResult;
            renderingResult = getRenderedSetFormula();
            if (renderingResult != null) {
                Point2D offset = getSetFormulaOffset();
                drawFormula(g, 2, offset, renderingResult);
            }
            renderingResult = getRenderedResetFormula();
            if (renderingResult != null) {
                Point2D offset = getResetFormulaOffset();
                drawFormula(g, 1, offset, renderingResult);
            }
        }
        super.draw(r);
    }

    private boolean needsFormulas() {
        boolean result = false;
        Node parent = getParent();
        if (parent != null) {
            // Primary input port
            if (!(parent instanceof VisualCircuitComponent) && isInput()) {
                result = true;
            }
            // Output port of a BOX-rendered component
            if ((parent instanceof VisualFunctionComponent) && isOutput()) {
                VisualFunctionComponent component = (VisualFunctionComponent) parent;
                if (component.getRenderType() == RenderType.BOX) {
                    result = true;
                }
            }
        }
        return result;
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        if (needsFormulas()) {
            bb = BoundingBoxHelper.union(bb, getSetBoundingBox());
            bb = BoundingBoxHelper.union(bb, getResetBoundingBox());
        }
        return bb;
    }

    private Collection<VisualFunctionContact> getAllContacts() {
        HashSet<VisualFunctionContact> result = new HashSet<>();
        Node root = Hierarchy.getRoot(this);
        if (root != null) {
            result.addAll(Hierarchy.getDescendantsOfType(root, VisualFunctionContact.class));
        }
        return result;
    }

    @Override
    public void notify(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pc = (PropertyChangedEvent) e;
            String propertyName = pc.getPropertyName();
            if (propertyName.equals(FunctionContact.PROPERTY_SET_FUNCTION) || propertyName.equals(FunctionContact.PROPERTY_RESET_FUNCTION)) {
                invalidateRenderedFormula();
            }
            if (propertyName.equals(Contact.PROPERTY_NAME)) {
                for (VisualFunctionContact vc : getAllContacts()) {
                    vc.invalidateRenderedFormula();
                }
            }
        }
        super.notify(e);
    }

}
