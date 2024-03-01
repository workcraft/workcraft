package org.workcraft.plugins.circuit;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.*;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.tools.StateDecoration;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.types.Pair;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VisualContact extends VisualComponent implements StateObserver, CustomTouchable {

    public static final String PROPERTY_DIRECTION = "Direction";

    public enum Direction {
        WEST("West"),
        NORTH("North"),
        EAST("East"),
        SOUTH("South");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public AffineTransform getTransform() {
            AffineTransform result = new AffineTransform();
            switch (this) {
            case WEST:
                result.quadrantRotate(2);
                break;
            case NORTH:
                result.quadrantRotate(3);
                break;
            case EAST:
                result.setToIdentity();
                break;
            case SOUTH:
                result.quadrantRotate(1);
                break;
            }
            return result;
        }

        public Direction rotateClockwise() {
            switch (this) {
            case WEST: return NORTH;
            case NORTH: return EAST;
            case EAST: return SOUTH;
            case SOUTH: return WEST;
            default: return this;
            }
        }

        public Direction rotateCounterclockwise() {
            switch (this) {
            case WEST: return SOUTH;
            case NORTH: return WEST;
            case EAST: return NORTH;
            case SOUTH: return EAST;
            default: return this;
            }
        }

        public Direction flipHorizontal() {
            switch (this) {
            case WEST: return EAST;
            case EAST: return WEST;
            default: return this;
            }
        }

        public Direction flipVertical() {
            switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            default: return this;
            }
        }

        public Direction flip() {
            switch (this) {
            case WEST: return EAST;
            case NORTH: return SOUTH;
            case EAST: return WEST;
            case SOUTH: return NORTH;
            default: return this;
            }
        }

        public int getGradientX() {
            switch (this) {
            case WEST: return -1;
            case EAST: return 1;
            default: return 0;
            }
        }

        public int getGradientY() {
            switch (this) {
            case NORTH: return -1;
            case SOUTH: return 1;
            default: return 0;
            }
        }
    }

    private static final double size = 0.3;
    public static final Font FANOUT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
    private static final double X_FANOUT_OFFSET = -0.25 * size;
    private static final double Y_FANOUT_OFFSET = -0.5 * size;

    private Direction direction = Direction.WEST;
    private RenderedText fanoutRenderedText = new RenderedText("", getFanoutFont(), Positioning.CENTER, getFanoutOffset());

    public VisualContact(Contact contact) {
        super(contact, true, false, false);
        setDefaultDirection();
        contact.addObserver(this);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Direction.class, PROPERTY_DIRECTION,
                this::setDirection, this::getDirection) {
            @Override
            public boolean isVisible() {
                return isPort();
            }
        }.setCombinable());

        addPropertyDeclaration(new PropertyDeclaration<>(IOType.class, Contact.PROPERTY_IO_TYPE,
                value -> getReferencedComponent().setIOType(value),
                () -> getReferencedComponent().getIOType())
                .setCombinable());

        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, Contact.PROPERTY_INIT_TO_ONE,
                value -> getReferencedComponent().setInitToOne(value),
                () -> getReferencedComponent().getInitToOne()) {
            @Override
            public boolean isVisible() {
                return isDriver() && !isZeroDelayDriver();
            }
        }.setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, Contact.PROPERTY_FORCED_INIT,
                value -> getReferencedComponent().setForcedInit(value),
                () -> getReferencedComponent().getForcedInit()) {
            @Override
            public boolean isVisible() {
                return isDriver() && !isZeroDelayPin();
            }
        }.setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, Contact.PROPERTY_PATH_BREAKER,
                value -> getReferencedComponent().setPathBreaker(value),
                () -> getReferencedComponent().getPathBreaker()) {
            @Override
            public boolean isVisible() {
                return isPin() && !isZeroDelayDriver();
            }
        }.setCombinable().setTemplatable());
    }

    @NoAutoSerialisation
    public void setForcedInit(boolean value) {
        getReferencedComponent().setForcedInit(value);
    }

    @NoAutoSerialisation
    public boolean getForcedInit() {
        return getReferencedComponent().getForcedInit();
    }

    @NoAutoSerialisation
    public void setInitToOne(boolean value) {
        getReferencedComponent().setInitToOne(value);
    }

    @NoAutoSerialisation
    public boolean getInitToOne() {
        return getReferencedComponent().getInitToOne();
    }

    @Override
    public Shape getShape() {
        Contact contact = getReferencedComponent();
        return ((contact != null) && contact.isPort()) ? getPortShape() : getContactShape();
    }

    private Shape getForcedShape() {
        Contact contact = getReferencedComponent();
        return ((contact != null) && contact.isPort()) ? getPortShape() : getForcedContactShape();
    }

    private Shape getPortShape() {
        double w2 = 0.5 * (size - CircuitSettings.getWireWidth());
        double h2 = 0.5 * (size - CircuitSettings.getWireWidth());
        Path2D path = new Path2D.Double();
        path.moveTo(-w2, -h2);
        path.lineTo(0.0, -h2);
        path.lineTo(w2, 0.0);
        path.lineTo(0.0, h2);
        path.lineTo(-w2, h2);
        path.closePath();
        return path;
    }

    private Shape getContactShape() {
        double w2 = 0.5 * size - CircuitSettings.getWireWidth();
        double h2 = 0.5 * size - CircuitSettings.getWireWidth();
        Path2D path = new Path2D.Double();
        path.moveTo(-w2, -h2);
        path.lineTo(w2, -h2);
        path.lineTo(w2, h2);
        path.lineTo(-w2, h2);
        path.closePath();
        return path;
    }

    private Shape getForcedContactShape() {
        double d = 0.5 * (size - CircuitSettings.getWireWidth());
        Path2D path = new Path2D.Double();
        path.moveTo(-d, 0);
        path.lineTo(0, d);
        path.lineTo(d, 0);
        path.lineTo(0, -d);
        path.closePath();
        return path;
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        Color colorisation = d.getColorisation();
        Color fillColor = d.getBackground();
        if (fillColor == null) {
            fillColor = getFillColor();
        }

        AffineTransform savedTransform = g.getTransform();
        AffineTransform rotateTransform = getDirection() != null ? getDirection().getTransform() : new AffineTransform();
        if (isInput()) {
            rotateTransform.quadrantRotate(2);
        }
        g.transform(rotateTransform);

        boolean showContact = r.getModel().getMathModel().getConnections(this.getReferencedComponent()).isEmpty()
                || (d instanceof StateDecoration) || (d.getColorisation() != null) || (d.getBackground() != null);

        if (showContact || isPort()) {
            boolean showForcedInit = (d instanceof StateDecoration) && ((StateDecoration) d).showForcedInit();
            Shape shape = showForcedInit && isForcedDriver() ? getForcedShape() : getShape();
            boolean useBoldOutline = (d instanceof StateDecoration) && ((StateDecoration) d).useBoldOutline();
            float width = (float) CircuitSettings.getBorderWidth();
            if (useBoldOutline) {
                g.setStroke(new BasicStroke(2.5f * width));
                g.setColor(ColorUtils.colorise(getForegroundColor(), colorisation));
                g.draw(shape);
            }
            g.setColor(fillColor);
            g.fill(shape);
            if (!useBoldOutline) {
                g.setStroke(new BasicStroke(width));
                g.setColor(ColorUtils.colorise(getForegroundColor(), colorisation));
                g.draw(shape);
            }
        } else if (r.getModel().getConnections(this).size() > 1) {
            g.setColor(ColorUtils.colorise(getForegroundColor(), colorisation));
            g.fill(VisualJoint.shape);
        }

        if (CircuitSettings.getShowContactFanout() && isDriver()) {
            g.setTransform(savedTransform);
            if ((getDirection() == Direction.NORTH) || (getDirection() == Direction.SOUTH)) {
                rotateTransform.setToIdentity();
                rotateTransform.quadrantRotate(-1);
                g.transform(rotateTransform);
            }
            g.translate(0.0, -size / 2);
            drawFanoutInLocalSpace(r);
        }

        if (!(getParent() instanceof VisualCircuitComponent)) {
            g.setTransform(savedTransform);
            if ((getDirection() == Direction.NORTH) || (getDirection() == Direction.SOUTH)) {
                rotateTransform.setToIdentity();
                rotateTransform.quadrantRotate(-1);
                g.transform(rotateTransform);
            }
            drawNameInLocalSpace(r);
        }

        g.setTransform(savedTransform);
        d.decorate(g);
    }

    public Font getFanoutFont() {
        return FANOUT_FONT.deriveFont((float) CircuitSettings.getContactFontSize());
    }

    private Point2D getFanoutOffset() {
        double xOffset = X_FANOUT_OFFSET;
        Direction dir = getDirection();
        if (isPort()) {
            dir = dir.flip();
        }
        if ((dir == Direction.SOUTH) || (dir == Direction.WEST)) {
            xOffset = -X_FANOUT_OFFSET;
        }
        return new Point2D.Double(xOffset, Y_FANOUT_OFFSET);
    }

    protected void cacheFanoutRenderedText(DrawRequest r) {
        Pair<Integer, Boolean> fanout = CircuitUtils.calcFanout((VisualCircuit) r.getModel(), this);
        String fanoutText = fanout.getFirst() + (fanout.getSecond() ? "+" : "");
        cacheFanoutRenderedText(fanoutText, getFanoutFont());
    }

    protected void cacheFanoutRenderedText(String text, Font font) {
        if (fanoutRenderedText.isDifferent(text, font, Positioning.CENTER, getFanoutOffset())) {
            fanoutRenderedText = new RenderedText(text, font, Positioning.CENTER, getFanoutOffset());
        }
    }

    protected void drawFanoutInLocalSpace(DrawRequest r) {
        cacheFanoutRenderedText(r);
        fanoutRenderedText.draw(r.getGraphics());
    }

    @Override
    public Positioning getNamePositioning() {
        Positioning result = Positioning.CENTER;
        Direction direction = getDirection();
        if (direction != null) {
            if ((direction == Direction.NORTH) || (direction == Direction.EAST)) {
                result = Positioning.RIGHT;
            } else {
                result = Positioning.LEFT;
            }
        }
        return result;
    }

    @Override
    public Color getNameColor() {
        return isInput() ? SignalCommonSettings.getInputColor() : SignalCommonSettings.getOutputColor();
    }

    @Override
    public Rectangle2D getNameBoundingBox() {
        Rectangle2D bb = super.getNameBoundingBox();
        if (bb != null) {
            AffineTransform rotateTransform = new AffineTransform();
            if (getDirection() == Direction.NORTH || getDirection() == Direction.SOUTH) {
                rotateTransform.quadrantRotate(-1);
            }
            bb = BoundingBoxHelper.transform(bb, rotateTransform);
        }
        return bb;
    }

    @Override
    public boolean getNameVisibility() {
        return true;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        Shape shape = getShape();
        if (shape != null) {
            Point2D p2 = new Point2D.Double();
            p2.setLocation(pointInLocalSpace);
            if (!(getParent() instanceof VisualCircuitComponent)) {
                AffineTransform rotateTransform = getDirection().getTransform();
                if (isInput()) {
                    rotateTransform.quadrantRotate(2);
                }
                rotateTransform.transform(pointInLocalSpace, p2);
            }
            return shape.contains(p2);
        }
        return false;
    }

    @Override
    public Node hitCustom(Point2D point) {
        Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
        Rectangle2D bb = getNameBoundingBox();
        if (bb != null) {
            if (bb.contains(pointInLocalSpace)) {
                return this;
            }
        }
        return hitTestInLocalSpace(pointInLocalSpace) ? this : null;
    }

    public final void setDefaultDirection() {
        setDirection(getReferencedComponent().getIOType() == IOType.INPUT ? Direction.WEST : Direction.EAST);
    }

    public final void setDirection(Direction value) {
        if (direction != value) {
            sendNotification(new TransformChangingEvent(this));
            direction = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_DIRECTION));
            sendNotification(new TransformChangedEvent(this));
        }
    }

    public Direction getDirection() {
        return direction;
    }

    @NoAutoSerialisation
    public String getName() {
        return getReferencedComponent().getName();
    }

    @Override
    public Contact getReferencedComponent() {
        return (Contact) super.getReferencedComponent();
    }

    public boolean isInput() {
        return getReferencedComponent().isInput();
    }

    public boolean isOutput() {
        return getReferencedComponent().isOutput();
    }

    public boolean isPin() {
        return getReferencedComponent().isPin();
    }

    public boolean isPort() {
        return getReferencedComponent().isPort();
    }

    public boolean isDriver() {
        return getReferencedComponent().isDriver();
    }

    public boolean isDriven() {
        return getReferencedComponent().isDriven();
    }

    public boolean isForcedDriver() {
        return getReferencedComponent().isForcedDriver();
    }

    public boolean isZeroDelayPin() {
        return getReferencedComponent().isZeroDelayPin();
    }

    public boolean isZeroDelayDriver() {
        return getReferencedComponent().isZeroDelayDriver();
    }

    @Override
    public void notify(StateEvent e) {
    }

    @Override
    public void rotateClockwise() {
        if (getParent() instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) getParent();
            if (component.getRenderType() == RenderType.BOX) {
                AffineTransform rotateTransform = new AffineTransform();
                rotateTransform.quadrantRotate(1);
                Point2D pos = rotateTransform.transform(getPosition(), null);
                setPosition(pos);
            }
        }
        setDirection(getDirection().rotateClockwise());
        super.rotateClockwise();
    }

    @Override
    public void rotateCounterclockwise() {
        if (getParent() instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) getParent();
            if (component.getRenderType() == RenderType.BOX) {
                AffineTransform rotateTransform = new AffineTransform();
                rotateTransform.quadrantRotate(-1);
                Point2D pos = rotateTransform.transform(getPosition(), null);
                setPosition(pos);
            }
        }
        setDirection(getDirection().rotateCounterclockwise());
        super.rotateCounterclockwise();
    }

    @Override
    public void flipHorizontal() {
        if (getParent() instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) getParent();
            if (component.getRenderType() == RenderType.BOX) {
                setX(-getX());
            }
        }
        setDirection(getDirection().flipHorizontal());
        super.flipHorizontal();
    }

    @Override
    public void flipVertical() {
        if (getParent() instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) getParent();
            if (component.getRenderType() == RenderType.BOX) {
                setY(-getY());
            }
        }
        setDirection(getDirection().flipVertical());
        super.flipVertical();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualContact) {
            VisualContact srcComponent = (VisualContact) src;
            getReferencedComponent().setInitToOne(srcComponent.getReferencedComponent().getInitToOne());
            getReferencedComponent().setForcedInit(srcComponent.getReferencedComponent().getForcedInit());
            getReferencedComponent().setPathBreaker(srcComponent.getReferencedComponent().getPathBreaker());
            // TODO: Note that IOType and Direction are currently NOT copied to allow input/output
            //       port generation with Shift key (and not to be copied from a template node).
        }
    }

}
