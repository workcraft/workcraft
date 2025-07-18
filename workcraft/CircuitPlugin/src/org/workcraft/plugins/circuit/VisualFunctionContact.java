package org.workcraft.plugins.circuit;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.visitors.FormulaRenderingResult;
import org.workcraft.formula.visitors.FormulaToGraphics;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.util.Collection;
import java.util.HashSet;

@DisplayName("Input/output port")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/circuit-node-port.svg")
public class VisualFunctionContact extends VisualContact implements StateObserver {

    private enum ArrowType { UP, DOWN }

    private static final double FORMULA_OFFSET_SCALE = 0.9;
    private static final double ARROW_WIDTH_SCALE = 0.5;

    private static final Font functionFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
    private static final FontRenderContext context = new FontRenderContext(
            AffineTransform.getScaleInstance(1000.0, 1000.0), true, true);

    private FormulaRenderingResult renderedSetFunction = null;
    private FormulaRenderingResult renderedResetFunction = null;
    private static double functionFontSize = CircuitSettings.getContactFontSize();

    public VisualFunctionContact(FunctionContact contact) {
        super(contact);
    }

    @Override
    public FunctionContact getReferencedComponent() {
        return (FunctionContact) super.getReferencedComponent();
    }

    @NoAutoSerialisation
    public BooleanFormula getSetFunction() {
        return getReferencedComponent().getSetFunction();
    }

    @NoAutoSerialisation
    public void setSetFunction(BooleanFormula setFunction) {
        if (getParent() instanceof VisualFunctionComponent p) {
            p.invalidateRenderingResult();
        }
        renderedSetFunction = null;
        getReferencedComponent().setSetFunction(setFunction);
    }

    @NoAutoSerialisation
    public BooleanFormula getResetFunction() {
        return getReferencedComponent().getResetFunction();
    }

    @NoAutoSerialisation
    public void setResetFunction(BooleanFormula resetFunction) {
        if (getParent() instanceof VisualFunctionComponent p) {
            p.invalidateRenderingResult();
        }
        renderedResetFunction = null;
        getReferencedComponent().setResetFunction(resetFunction);
    }

    public void invalidateRenderedFormula() {
        renderedSetFunction = null;
        renderedResetFunction = null;
    }

    private Font getFunctionFont() {
        return functionFont.deriveFont((float) CircuitSettings.getContactFontSize());
    }

    private FormulaRenderingResult getRenderedSetFunction() {
        if (Math.abs(CircuitSettings.getContactFontSize() - functionFontSize) > 0.001) {
            functionFontSize = CircuitSettings.getContactFontSize();
            renderedSetFunction = null;
        }
        BooleanFormula setFunction = getReferencedComponent().getSetFunction();
        if (setFunction == null) {
            renderedSetFunction = null;
        } else if (renderedSetFunction == null) {
            renderedSetFunction = FormulaToGraphics.render(setFunction, context, getFunctionFont());
        }
        return renderedSetFunction;
    }

    private double getFormulaScaleOffset() {
        return getFanoutVisibility() ? FORMULA_OFFSET_SCALE : 0.0;
    }

    private Point2D getSetFormulaOffset() {
        double s = CircuitSettings.getContactFontSize();
        double xOffset = (getFormulaScaleOffset() + ARROW_WIDTH_SCALE) * s;
        double yOffset = -0.4 * CircuitSettings.getContactFontSize();
        FormulaRenderingResult renderingResult = getRenderedSetFunction();
        if (renderingResult != null) {
            Direction dir = getDirection();
            if (isPort()) {
                dir = dir.flip();
            }
            if ((dir == Direction.SOUTH) || (dir == Direction.WEST)) {
                xOffset = -s * getFormulaScaleOffset() - renderingResult.boundingBox.getWidth();
            }
        }
        return new Point2D.Double(xOffset, yOffset);
    }

    private Rectangle2D getSetBoundingBox() {
        Rectangle2D bb = null;
        FormulaRenderingResult setRenderingResult = getRenderedSetFunction();
        if (setRenderingResult != null) {
            bb = BoundingBoxHelper.move(setRenderingResult.boundingBox, getSetFormulaOffset());
            Direction dir = getDirection();
            if (isPort()) {
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

    private FormulaRenderingResult getRenderedResetFunction() {
        if (Math.abs(CircuitSettings.getContactFontSize() - functionFontSize) > 0.001) {
            functionFontSize = CircuitSettings.getContactFontSize();
            renderedResetFunction = null;
        }
        BooleanFormula resetFunction = getReferencedComponent().getResetFunction();
        if (resetFunction == null) {
            renderedResetFunction = null;
        } else if (renderedResetFunction == null) {
            renderedResetFunction = FormulaToGraphics.render(resetFunction, context, getFunctionFont());
        }
        return renderedResetFunction;
    }

    private Point2D getResetFormulaOffset() {
        double s = CircuitSettings.getContactFontSize();
        double xOffset = (getFormulaScaleOffset() + ARROW_WIDTH_SCALE) * s;
        double yOffset = 0.3 * s;
        FormulaRenderingResult renderingResult = getRenderedResetFunction();
        if (renderingResult != null) {
            Direction dir = getDirection();
            if (!(getParent() instanceof VisualFunctionComponent)) {
                dir = dir.flip();
            }
            if ((dir == Direction.SOUTH) || (dir == Direction.WEST)) {
                xOffset = -s * getFormulaScaleOffset() - renderingResult.boundingBox.getWidth();
            }
            yOffset -= renderingResult.getBoundingBox().getY();
        }
        return new Point2D.Double(xOffset, yOffset);
    }

    private Rectangle2D getResetBoundingBox() {
        Rectangle2D bb = null;
        FormulaRenderingResult renderingResult = getRenderedResetFunction();
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

    @SuppressWarnings("PMD.SwitchDensity")
    private void drawArrow(Graphics2D g, ArrowType arrowType, Point2D offset) {
        double s = CircuitSettings.getContactFontSize();
        float lineStrokeWidth = 0.08f * (float) s;
        g.setStroke(new BasicStroke(lineStrokeWidth));
        double middleX = offset.getX() - ARROW_WIDTH_SCALE * s;
        double middleY = offset.getY() - 0.3 * s;
        double lineHeight = 0.3 * s;
        double headHeight = 0.3 * s;
        double headWidth = 0.15 * s;
        switch (arrowType) {
            case UP:
                double upLineY = offset.getY() - 0.5 * lineStrokeWidth;
                double upHeadY = upLineY - lineHeight;
                Path2D upHeadPath = new Path2D.Double();
                upHeadPath.moveTo(middleX - headWidth, upHeadY);
                upHeadPath.lineTo(middleX + headWidth, upHeadY);
                upHeadPath.lineTo(middleX, upHeadY - headHeight);
                upHeadPath.closePath();
                g.fill(upHeadPath);
                g.draw(new Line2D.Double(middleX, upLineY, middleX, upHeadY));
                break;
            case DOWN:
                double downHeadY = offset.getY() - headHeight;
                double downLineY = downHeadY - lineHeight;
                Path2D downHeadPath = new Path2D.Double();
                downHeadPath.moveTo(middleX - headWidth, downHeadY);
                downHeadPath.lineTo(middleX + headWidth, downHeadY);
                downHeadPath.lineTo(middleX, downHeadY + headHeight);
                downHeadPath.closePath();
                g.fill(downHeadPath);
                g.draw(new Line2D.Double(middleX, downLineY, middleX, downHeadY));
                break;
        }
        float columnStrokeWidth = 1.25f * lineStrokeWidth;
        g.setStroke(new BasicStroke(columnStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        double columnX = middleX + 0.25 * s;
        double columnTopY = middleY - 0.1 * s;
        double columnBottomY = offset.getY() - 0.5 * columnStrokeWidth;
        g.draw(new Line2D.Double(columnX, columnTopY, columnX, columnTopY));
        g.draw(new Line2D.Double(columnX, columnBottomY, columnX, columnBottomY));
    }

    private void drawFormula(Graphics2D g, ArrowType arrowType, Point2D offset, FormulaRenderingResult renderingResult) {
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

            drawArrow(g, arrowType, offset);

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
            g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
            FormulaRenderingResult renderingResult;
            renderingResult = getRenderedSetFunction();
            if (renderingResult != null) {
                drawFormula(g, ArrowType.UP, getSetFormulaOffset(), renderingResult);
            }
            renderingResult = getRenderedResetFunction();
            if (renderingResult != null) {
                drawFormula(g, ArrowType.DOWN, getResetFormulaOffset(), renderingResult);
            }
        }
        super.draw(r);
    }

    private boolean needsFormulas() {
        boolean result = false;
        Node parent = getParent();
        if ((parent != null) && CircuitSettings.getShowContactFunctions()) {
            // Primary input port
            if (!(parent instanceof VisualCircuitComponent) && isInput()) {
                result = true;
            }
            // Output port of a BOX-rendered component
            if ((parent instanceof VisualFunctionComponent component) && isOutput()) {
                if (component.getRenderingResult() == null) {
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
        if (e instanceof PropertyChangedEvent pc) {
            String propertyName = pc.getPropertyName();
            if (propertyName.equals(FunctionContact.PROPERTY_FUNCTION)) {
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
