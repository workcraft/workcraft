package org.workcraft.plugins.circuit;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.MixUtils;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.circuit.renderers.CElementRenderer;
import org.workcraft.plugins.circuit.renderers.CElementRenderingResult;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.renderers.GateRenderer;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@DisplayName("Function Component")
@Hotkey(KeyEvent.VK_F)
@SVGIcon("images/circuit-node-function.svg")
public class VisualFunctionComponent extends VisualCircuitComponent {

    private RenderType renderType = RenderType.GATE;
    private ComponentRenderingResult renderingResult = null;
    private boolean isValidRenderingCache = false;

    public VisualFunctionComponent(FunctionComponent component) {
        super(component);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(RenderType.class,
                PROPERTY_RENDER_TYPE,
                this::setRenderType, this::getRenderType)
                .setCombinable());

        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class,
                FunctionComponent.PROPERTY_IS_ZERO_DELAY,
                this::setIsZeroDelay, this::getIsZeroDelay));
    }

    @Override
    public FunctionComponent getReferencedComponent() {
        return (FunctionComponent) super.getReferencedComponent();
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public void setRenderType(RenderType value) {
        if (renderType != value) {
            renderType = value;
            invalidateRenderingResult();
            invalidateBoundingBox();
            setContactsDefaultPosition();
            sendNotification(new PropertyChangedEvent(this, PROPERTY_RENDER_TYPE));
        }
    }

    @NoAutoSerialisation
    public boolean getIsZeroDelay() {
        return (getReferencedComponent() != null) && getReferencedComponent().getIsZeroDelay();
    }

    @NoAutoSerialisation
    public void setIsZeroDelay(boolean value) {
        if (getReferencedComponent() != null) {
            getReferencedComponent().setIsZeroDelay(value);
        }
    }

    public boolean isMapped() {
        return (getReferencedComponent() != null) && getReferencedComponent().isMapped();
    }

    public void clearMapping() {
        getReferencedComponent().setModule("");
    }

    public boolean isGate() {
        return (getReferencedComponent() != null) && getReferencedComponent().isGate();
    }

    public boolean isBuffer() {
        return (getReferencedComponent() != null) && getReferencedComponent().isBuffer();
    }

    public boolean isInverter() {
        return (getReferencedComponent() != null) && getReferencedComponent().isInverter();
    }

    public VisualFunctionContact getGateOutput() {
        if (getReferencedComponent() != null) {
            FunctionContact contact = getReferencedComponent().getGateOutput();
            return getVisualContact(contact);
        }
        return null;
    }

    public boolean isSequentialGate() {
        return (getReferencedComponent() != null) && getReferencedComponent().isSequentialGate();
    }

    public Collection<VisualFunctionContact> getVisualFunctionContacts() {
        return Hierarchy.filterNodesByType(getChildren(), VisualFunctionContact.class);
    }

    @Override
    public VisualFunctionContact createContact(Contact.IOType ioType) {
        VisualFunctionContact vc = new VisualFunctionContact(new FunctionContact(ioType));
        addContact(vc);
        return vc;
    }

    public VisualFunctionContact getVisualContact(Contact contact) {
        for (Node node: getChildren()) {
            if (!(node instanceof VisualFunctionContact)) continue;
            VisualFunctionContact visualContact = (VisualFunctionContact) node;
            if (visualContact.getReferencedComponent() == contact) {
                return visualContact;
            }
        }
        return null;
    }

    @Override
    public VisualFunctionContact getMainVisualOutput() {
        VisualFunctionContact result = null;
        for (VisualFunctionContact contact: getVisualFunctionContacts()) {
            if (contact.isOutput()) {
                if (result == null) {
                    result = contact;
                } else {
                    return null;
                }
            }
        }
        return result;
    }

    @Override
    public VisualFunctionContact getFirstVisualInput() {
        VisualFunctionContact result = null;
        for (VisualFunctionContact contact: getVisualFunctionContacts()) {
            if (contact.isInput()) {
                result = contact;
                break;
            }
        }
        return result;
    }

    @Override
    public VisualFunctionContact getFirstVisualOutput() {
        VisualFunctionContact result = null;
        for (VisualFunctionContact contact: getVisualFunctionContacts()) {
            if (contact.isOutput()) {
                result = contact;
                break;
            }
        }
        return result;
    }

    public ComponentRenderingResult getRenderingResult() {
        if ((groupImpl == null) || getReferencedComponent().getIsArbitrationPrimitive()) {
            return null;
        }
        if (!isValidRenderingCache) {
            renderingResult = null;
            if ((getRenderType() == RenderType.GATE) && isGate()) {
                GateRenderer.foregroundColor = getForegroundColor();
                GateRenderer.backgroundColor = getFillColor();
                VisualFunctionContact gateOutput = getGateOutput();
                BooleanFormula setFunction = gateOutput.getSetFunction();
                if ((setFunction != null) && !CircuitUtils.isConstant(setFunction)) {
                    BooleanFormula resetFunction = gateOutput.getResetFunction();
                    if (resetFunction == null) {
                        renderingResult = GateRenderer.renderGate(setFunction);
                    } else {
                        renderingResult = CElementRenderer.renderGate(setFunction, resetFunction);
                    }
                }
            }
            isValidRenderingCache = true;
        }
        return renderingResult;
    }

    public void invalidateRenderingResult() {
        isValidRenderingCache = false;
        renderingResult = null;
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        ComponentRenderingResult renderingResult = getRenderingResult();
        if (renderingResult == null) {
            return super.getInternalBoundingBoxInLocalSpace();
        } else {
            Rectangle2D boundingBox = renderingResult.getBoundingBox();
            AffineTransform at = getMainContactRotateTransform(false);
            return at.createTransformedShape(boundingBox).getBounds2D();
        }
    }

    private AffineTransform getMainContactRotateTransform(boolean reverse) {
        AffineTransform at = new AffineTransform();
        VisualContact contact = getMainVisualOutput();
        if (contact != null) {
            switch (contact.getDirection()) {
            case NORTH:
                at.quadrantRotate(reverse ? 1 : 3);
                break;
            case SOUTH:
                at.quadrantRotate(reverse ? 3 : 1);
                break;
            case WEST:
                at.quadrantRotate(2);
                break;
            case EAST:
                break;
            }
        }
        return at;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        ComponentRenderingResult res = getRenderingResult();
        if (res == null) {
            return super.hitTestInLocalSpace(pointInLocalSpace);
        } else {
            return res.getBoundingBox().contains(pointInLocalSpace);
        }
    }

    @Override
    public void setContactsDefaultPosition() {
        ComponentRenderingResult res = getRenderingResult();
        if (res == null) {
            super.setContactsDefaultPosition();
        } else {
            AffineTransform at = new AffineTransform();
            AffineTransform bt = new AffineTransform();
            VisualContact mainOutput = getMainVisualOutput();
            if (mainOutput != null) {
                Direction direction = mainOutput.getDirection();
                at = direction != null ? direction.getTransform() : new AffineTransform();
            }
            double inputPositionX = TransformHelper.snapP5(res.getBoundingBox().getMinX() - GateRenderer.contactMargin);
            double outputPositionX = TransformHelper.snapP5(res.getBoundingBox().getMaxX() + GateRenderer.contactMargin);

            for (Node node: this.getChildren()) {
                if (node instanceof VisualFunctionContact) {
                    VisualFunctionContact contact = (VisualFunctionContact) node;
                    bt.setTransform(at);
                    if (contact.isInput()) {
                        String vcName = contact.getName();
                        List<Point2D> positions = res.getContactPositions().get(vcName);
                        if (positions != null) {
                            Point2D position = MixUtils.bestPoint(positions, Math::min);
                            bt.translate(inputPositionX, position.getY());
                        }
                    } else {
                        bt.translate(outputPositionX, 0);
                    }
                    // Here we only need to change position, do not do the rotation
                    AffineTransform ct = new AffineTransform();
                    ct.translate(bt.getTranslateX(), bt.getTranslateY());
                    contact.setTransform(ct);
                }
            }
        }
    }

    @Override
    public void addContact(VisualContact vc) {
        super.addContact(vc);
        invalidateRenderingResult();
    }

    @Override
    public void notify(StateEvent e) {
        super.notify(e);

        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pc = (PropertyChangedEvent) e;
            String propertyName = pc.getPropertyName();
            if (propertyName.equals(Contact.PROPERTY_NAME)) {
                // This is needed to recalculate the map of contact name to position.
                invalidateRenderingResult();
            }
            if (propertyName.equals(VisualContact.PROPERTY_DIRECTION)) {
                VisualContact mainContact = getMainVisualOutput();
                if ((mainContact == pc.getSender()) && (getRenderingResult() != null)) {
                    setContactsDefaultPosition();
                }
            }
            if (propertyName.equals(FunctionContact.PROPERTY_FUNCTION)) {
                setContactsDefaultPosition();
                for (Node node : getChildren()) {
                    if (node instanceof VisualFunctionContact) {
                        VisualFunctionContact vc = (VisualFunctionContact) node;
                        vc.invalidateRenderedFormula();
                    }
                }
            }
        }
    }

    private Point2D getContactLinePositionInLocalSpace(VisualFunctionContact vc, ComponentRenderingResult rr) {
        Point2D pinPosition = null;
        if (vc.isInput()) {
            String cname = vc.getReferencedComponent().getName();
            List<Point2D> positions = rr.getContactPositions().get(cname);
            if (positions != null) {
                Point2D position = MixUtils.bestPoint(positions, Math::min);
                pinPosition = new Point2D.Double(position.getX(), position.getY());
            }
        } else {
            pinPosition = new Point2D.Double(rr.getBoundingBox().getMaxX(), 0.0);
        }
        return pinPosition;
    }

    private void drawContactLines(Graphics2D g, ComponentRenderingResult rr, AffineTransform at) {
        g.setStroke(new BasicStroke((float) CircuitSettings.getWireWidth()));
        g.setColor(GateRenderer.foregroundColor);
        Map<String, List<Point2D>> contactToPositions = rr.getContactPositions();
        for (Node node : this.getChildren()) {
            if (node instanceof VisualFunctionContact) {
                VisualFunctionContact contact = (VisualFunctionContact) node;
                Point2D contactPosition = contact.getPosition();
                String literal = contact.getName();
                List<Point2D> positions = contact.isInput() ? contactToPositions.getOrDefault(literal, Collections.emptyList())
                        : Collections.singletonList(new Point2D.Double(rr.getBoundingBox().getMaxX(), 0.0));

                for (Point2D position : positions) {
                    Point2D transformPosition = at.transform(position, null);
                    Line2D line = new Line2D.Double(contactPosition, transformPosition);
                    g.draw(line);
                }
                if (positions.size() > 1) {
                    AffineTransform bt = new AffineTransform();
                    bt.translate(contactPosition.getX(), contactPosition.getY());
                    g.fill(bt.createTransformedShape(VisualJoint.shape));
                }
            }
        }
    }

    private void drawCelementSymbols(Graphics2D g, CElementRenderingResult cr, AffineTransform at) {
        Point2D labelPosition = cr.getLabelPosition();
        if (labelPosition != null) {
            at.transform(labelPosition, labelPosition);
            g.draw(getCShape(labelPosition));
        }

        Point2D plusPosition = cr.getPlusPosition();
        if (plusPosition != null) {
            at.transform(plusPosition, plusPosition);
            g.draw(getPlusShape(plusPosition));
        }

        Point2D minusPosition = cr.getMinusPosition();
        if (minusPosition != null) {
            at.transform(minusPosition, minusPosition);
            g.draw(getMinusShape(minusPosition));
        }
    }

    private Arc2D.Double getCShape(Point2D pos) {
        return new Arc2D.Double(pos.getX() - 0.15, pos.getY() - 0.15, 0.30, 0.30, 60, 240, Arc2D.OPEN);
    }

    private Path2D getPlusShape(Point2D pos) {
        Path2D plusShape = new Path2D.Double();
        plusShape.moveTo(pos.getX() - 0.10, pos.getY());
        plusShape.lineTo(pos.getX() + 0.10, pos.getY());
        plusShape.moveTo(pos.getX(), pos.getY() - 0.10);
        plusShape.lineTo(pos.getX(), pos.getY() + 0.10);
        return plusShape;
    }

    private Line2D getMinusShape(Point2D pos) {
        return new Line2D.Double(pos.getX() - 0.10, pos.getY(), pos.getX() + 0.10, pos.getY());
    }

    private void drawBypass(Graphics2D g, ComponentRenderingResult rr, AffineTransform at) {
        Point2D inputPos = null;
        Point2D outputPos = null;
        for (Node node: this.getChildren()) {
            if (node instanceof VisualFunctionContact) {
                VisualFunctionContact vc = (VisualFunctionContact) node;
                if (vc.isInput()) {
                    inputPos = getContactLinePositionInLocalSpace(vc, rr);
                } else {
                    outputPos = getContactLinePositionInLocalSpace(vc, rr);
                }
            }
        }
        if ((inputPos != null) && (outputPos != null)) {
            float[] pattern = {0.1f, 0.1f};
            g.setStroke(new BasicStroke((float) CircuitSettings.getBorderWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));

            g.setColor(GateRenderer.foregroundColor);
            at.transform(inputPos, inputPos);
            at.transform(outputPos, outputPos);
            Line2D line = new Line2D.Double(inputPos, outputPos);
            g.draw(line);
        }
    }

    @Override
    public void draw(DrawRequest r) {
        ComponentRenderingResult rr = getRenderingResult();
        if (rr == null) {
            super.draw(r);
            if (getReferencedComponent().getIsArbitrationPrimitive()) {
                drawShield(r);
            }
        } else {
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            cacheRenderedText(r); // needed to better estimate the bounding box

            // Determine rotation by the direction of the main contact (usually the only output)
            AffineTransform at = getMainContactRotateTransform(false);

            // Draw the component in its coordinates
            g.transform(at);
            GateRenderer.foregroundColor = ColorUtils.colorise(getForegroundColor(), r.getDecoration().getColorisation());
            GateRenderer.backgroundColor = ColorUtils.colorise(getFillColor(), r.getDecoration().getBackground());
            setStroke(g);
            rr.draw(g);
            AffineTransform bt = getMainContactRotateTransform(true);
            g.transform(bt);

            if ((isBuffer() || isInverter()) && getIsZeroDelay()) {
                drawBypass(g, rr, at);
            }

            drawContactLines(g, rr, at);
            if (rr instanceof CElementRenderingResult) {
                drawCelementSymbols(g, (CElementRenderingResult) rr, at);
            }
            drawLabelInLocalSpace(r);
            drawNameInLocalSpace(r);

            // External decorations
            d.decorate(g);
        }
    }

    private void drawShield(DrawRequest r) {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (bb != null) {
            Decoration d = r.getDecoration();
            Graphics2D g = r.getGraphics();
            g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));

            double x = bb.getCenterX();
            double y = bb.getCenterY() - 0.4 * VisualCommonSettings.getNodeSize();
            double w = 0.4 * VisualCommonSettings.getNodeSize();
            double w2 = w / 2;
            double b = 0.4 * VisualCommonSettings.getNodeSize();
            double t = b / 4;
            g.setStroke(new BasicStroke((float) CircuitSettings.getWireWidth()));

            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y);
            path.curveTo(x + 0.5 * w2, y - 0.2 * b, x + w2, y - 0.5 * b, x + w2, y - b);
            path.curveTo(x + 0.5 * w2, y - b, x + 0.5 * w2, y - b, x, y - b - t);
            path.curveTo(x - 0.5 * w2, y - b, x - 0.5 * w2, y - b, x - w2, y - b);
            path.curveTo(x - w2, y - 0.5 * b, x - 0.5 * w2, y - 0.2 * b, x, y);
            path.closePath();
            g.draw(path);
        }
    }

    @Override
    public void drawNameInLocalSpace(DrawRequest r) {
        if (!getIsZeroDelay() || CircuitSettings.getShowZeroDelayNames()) {
            super.drawNameInLocalSpace(r);
        }
    }

    @Override
    public void add(Node node) {
        super.add(node);
        if (node instanceof VisualContact) {
            invalidateRenderingResult();
        }
    }

    @Override
    public void remove(Node node) {
        if (node instanceof VisualContact) {
            invalidateRenderingResult();
        }
        super.remove(node);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualFunctionComponent) {
            VisualFunctionComponent srcComponent = (VisualFunctionComponent) src;
            setIsZeroDelay(srcComponent.getIsZeroDelay());
        }
    }

}
