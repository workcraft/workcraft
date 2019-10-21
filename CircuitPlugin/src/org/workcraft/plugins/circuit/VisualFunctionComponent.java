package org.workcraft.plugins.circuit;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.circuit.renderers.CElementRenderer;
import org.workcraft.plugins.circuit.renderers.CElementRenderingResult;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.renderers.GateRenderer;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.Coloriser;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.Collection;

@DisplayName("Function Component")
@Hotkey(KeyEvent.VK_F)
@SVGIcon("images/circuit-node-function.svg")
public class VisualFunctionComponent extends VisualCircuitComponent {

    private RenderType renderType = RenderType.GATE;
    private ComponentRenderingResult renderingResult = null;

    public VisualFunctionComponent(FunctionComponent component) {
        super(component);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(RenderType.class,
                PROPERTY_RENDER_TYPE,
                this::setRenderType, this::getRenderType)
                .setCombinable().setTemplatable());

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

    public VisualFunctionContact getVisualContact(Contact contact) {
        for (Node node: getChildren()) {
            if (!(node instanceof VisualFunctionContact)) continue;
            VisualFunctionContact visualContact = (VisualFunctionContact) node;
            if (visualContact.getReferencedContact() == contact) {
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

    private ComponentRenderingResult getRenderingResult() {
        if (groupImpl == null) return null;
        if (renderingResult != null) {
            return renderingResult;
        }
        VisualFunctionContact gateOutput = getGateOutput();
        // If gate output found render its visual representation according to the set and reset functions
        if (gateOutput != null) {
            switch (getRenderType()) {
            case BOX:
                renderingResult = null;
                break;
            case GATE:
                GateRenderer.foreground = getForegroundColor();
                GateRenderer.background = getFillColor();
                BooleanFormula setFunction = gateOutput.getSetFunction();
                if ((setFunction == null) || setFunction.equals(Zero.getInstance()) || setFunction.equals(One.getInstance())) {
                    renderingResult = null;
                } else {
                    BooleanFormula resetFunction = gateOutput.getResetFunction();
                    if (resetFunction == null) {
                        renderingResult = GateRenderer.renderGate(setFunction);
                    } else {
                        renderingResult = CElementRenderer.renderGate(setFunction, resetFunction);
                    }
                }
                break;
            }
        }
        return renderingResult;
    }

    public void invalidateRenderingResult() {
        renderingResult = null;
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        ComponentRenderingResult renderingResult = getRenderingResult();
        if (renderingResult == null) {
            return super.getInternalBoundingBoxInLocalSpace();
        } else {
            Rectangle2D boundingBox = renderingResult.boundingBox();
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
            return res.boundingBox().contains(pointInLocalSpace);
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
            double inputPositionX = TransformHelper.snapP5(res.boundingBox().getMinX() - GateRenderer.contactMargin);
            double outputPositionX = TransformHelper.snapP5(res.boundingBox().getMaxX() + GateRenderer.contactMargin);

            for (Node node: this.getChildren()) {
                if (node instanceof VisualFunctionContact) {
                    VisualFunctionContact contact = (VisualFunctionContact) node;
                    bt.setTransform(at);
                    if (contact.isInput()) {
                        String vcName = contact.getName();
                        Point2D position = res.contactPositions().get(vcName);
                        if (position != null) {
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
            if (propertyName.equals(FunctionContact.PROPERTY_SET_FUNCTION)
                    || propertyName.equals(FunctionContact.PROPERTY_RESET_FUNCTION)) {
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
            String cname = vc.getReferencedContact().getName();
            Point2D p = rr.contactPositions().get(cname);
            if (p != null) {
                pinPosition = new Point2D.Double(p.getX(), p.getY());
            }
        } else {
            pinPosition = new Point2D.Double(rr.boundingBox().getMaxX(), 0.0);
        }
        return pinPosition;
    }

    private void drawContactLines(Graphics2D g, ComponentRenderingResult rr, AffineTransform at) {
        g.setStroke(new BasicStroke((float) CircuitSettings.getWireWidth()));
        g.setColor(GateRenderer.foreground);
        for (Node node: this.getChildren()) {
            if (node instanceof VisualFunctionContact) {
                VisualFunctionContact vc = (VisualFunctionContact) node;
                Point2D p1 = getContactLinePositionInLocalSpace(vc, rr);
                if (p1 != null) {
                    at.transform(p1, p1);
                    Point2D p2 = vc.getPosition();
                    Line2D line = new Line2D.Double(p1, p2);
                    g.draw(line);
                }
            }
        }
    }

    private void drawCelementSymbols(Graphics2D g, ComponentRenderingResult rr, AffineTransform at) {
        if (rr instanceof CElementRenderingResult) {
            CElementRenderingResult cr = (CElementRenderingResult) rr;
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

            g.setColor(GateRenderer.foreground);
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
        } else {
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            cacheRenderedText(r); // needed to better estimate the bounding box

            // Determine rotation by the direction of the main contact (usually the only output)
            AffineTransform at = getMainContactRotateTransform(false);

            // Draw the component in its coordinates
            g.transform(at);
            GateRenderer.foreground = Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation());
            GateRenderer.background = Coloriser.colorise(getFillColor(), r.getDecoration().getBackground());
            setStroke(g);
            rr.draw(g);
            AffineTransform bt = getMainContactRotateTransform(true);
            g.transform(bt);

            if ((isBuffer() || isInverter()) && getIsZeroDelay()) {
                drawBypass(g, rr, at);
            }

            drawContactLines(g, rr, at);
            drawCelementSymbols(g, rr, at);
            drawLabelInLocalSpace(r);
            drawNameInLocalSpace(r);

            // External decorations
            d.decorate(g);
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
