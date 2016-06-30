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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.tools.StateDecoration;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualContact extends VisualComponent implements StateObserver {

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

        public static AffineTransform getDirectionTransform(Direction dir) {
            AffineTransform result = new AffineTransform();
            if (dir != null) {
                switch (dir) {
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

    };

    public static final Color inputColor = Color.RED;
    public static final Color outputColor = Color.BLUE;

    private static final double size = 0.3;
    private Direction direction = Direction.WEST;

    public VisualContact(Contact contact) {
        super(contact, true, false, false);
        setDirection(contact.getIOType() == IOType.INPUT ? Direction.WEST : Direction.EAST);
        contact.addObserver(this);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualContact, Direction>(
                this, PROPERTY_DIRECTION, Direction.class, true, true, true) {
            protected void setter(VisualContact object, Direction value) {
                object.setDirection(value);
            }
            protected Direction getter(VisualContact object) {
                return object.getDirection();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualContact, IOType>(
                this, Contact.PROPERTY_IO_TYPE, IOType.class, true, true, false) {
            protected void setter(VisualContact object, IOType value) {
                object.getReferencedContact().setIOType(value);
            }
            protected IOType getter(VisualContact object) {
                return object.getReferencedContact().getIOType();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualContact, Boolean>(
                this, Contact.PROPERTY_INIT_TO_ONE, Boolean.class, true, true, true) {
            protected void setter(VisualContact object, Boolean value) {
                object.getReferencedContact().setInitToOne(value);
            }
            protected Boolean getter(VisualContact object) {
                return object.getReferencedContact().getInitToOne();
            }
            public boolean isDisabled() {
                VisualContact contact = (VisualContact) getObject();
                return contact.isDriven();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualContact, Boolean>(
                this, Contact.PROPERTY_FORCED_INIT, Boolean.class, true, true, true) {
            protected void setter(VisualContact object, Boolean value) {
                object.getReferencedContact().setForcedInit(value);
            }
            protected Boolean getter(VisualContact object) {
                return object.getReferencedContact().getForcedInit();
            }
            public boolean isDisabled() {
                VisualContact contact = (VisualContact) getObject();
                return contact.isDriven();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualContact, Boolean>(
                this, Contact.PROPERTY_PATH_BREAKER, Boolean.class, true, true, true) {
            protected void setter(VisualContact object, Boolean value) {
                object.getReferencedContact().setPathBreaker(value);
            }
            protected Boolean getter(VisualContact object) {
                return object.getReferencedContact().getPathBreaker();
            }
            public boolean isDisabled() {
                VisualContact contact = (VisualContact) getObject();
                return contact.isDriver();
            }
        });
    }

    private Shape getShape() {
        Contact contact = getReferencedContact();
        if ((contact != null) && contact.isPort()) {
            return getPortShape();
        }
        return getContactShape();
    }

    private Shape getInitShape() {
        Contact contact = getReferencedContact();
        if (contact != null) {
            if (contact.isPort()) {
                return getPortShape();
            } else if (contact.isDriver() && contact.getForcedInit()) {
                return getInitialisedContactShape();
            }
        }
        return getContactShape();
    }

    private Shape getPortShape() {
        double d = 0.5 * (size - CircuitSettings.getWireWidth());
        Path2D path = new Path2D.Double();
        path.moveTo(-d, -d);
        path.lineTo(0, -d);
        path.lineTo(d, 0);
        path.lineTo(0, d);
        path.lineTo(-d, d);
        path.closePath();
        return path;
    }

    private Shape getContactShape() {
        double d = 0.5 * size - CircuitSettings.getWireWidth();
        Path2D path = new Path2D.Double();
        path.moveTo(-d, -d);
        path.lineTo(d, -d);
        path.lineTo(d, d);
        path.lineTo(-d, d);
        path.closePath();
        return path;
    }

    private Shape getInitialisedContactShape() {
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
        AffineTransform rotateTransform = Direction.getDirectionTransform(getDirection());
        if (isInput()) {
            rotateTransform.quadrantRotate(2);
        }
        g.transform(rotateTransform);

        boolean showContact = CircuitSettings.getShowContacts() || (d instanceof StateDecoration)
                || (d.getColorisation() != null) || (d.getBackground() != null);

        if (showContact || isPort()) {
            boolean showForcedInit = false;
            if (d instanceof StateDecoration) {
                showForcedInit = ((StateDecoration) d).showForcedInit();
            }
            Shape shape = showForcedInit ? getInitShape() : getShape();
            g.setStroke(new BasicStroke((float) CircuitSettings.getWireWidth()));
            g.setColor(fillColor);
            g.fill(shape);
            g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
            g.draw(shape);
        } else if (r.getModel().getConnections(this).size() > 1) {
            g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
            g.fill(VisualJoint.shape);
        }
        g.setTransform(savedTransform);

        if (!(getParent() instanceof VisualCircuitComponent)) {
            rotateTransform.setToIdentity();
            if (getDirection() == Direction.NORTH || getDirection() == Direction.SOUTH) {
                rotateTransform.quadrantRotate(-1);
            }
            g.transform(rotateTransform);
            drawNameInLocalSpace(r);
        }
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
        return isInput() ? inputColor : outputColor;
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
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return getShape().getBounds2D();
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        Point2D p2 = new Point2D.Double();
        p2.setLocation(pointInLocalSpace);
        if (!(getParent() instanceof VisualCircuitComponent)) {
            AffineTransform rotateTransform = new AffineTransform();
            // rotate in the direction opposite to Direction.getDirectionTransform
            switch (getDirection()) {
            case WEST:
                rotateTransform.quadrantRotate(2);
                break;
            case NORTH:
                rotateTransform.quadrantRotate(1);
                break;
            case EAST:
                rotateTransform.setToIdentity();
                break;
            case SOUTH:
                rotateTransform.quadrantRotate(3);
                break;
            }
            if (isInput()) {
                rotateTransform.quadrantRotate(2);
            }
            rotateTransform.transform(pointInLocalSpace, p2);
        }
        Shape shape = getShape();
        if (shape != null) {
            return shape.contains(p2);
        } else {
            return false;
        }
    }

    public void setDirection(Direction value) {
        if (value != direction) {
            sendNotification(new TransformChangingEvent(this));
            this.direction = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_DIRECTION));
            sendNotification(new TransformChangedEvent(this));
        }
    }

    public Direction getDirection() {
        return direction;
    }

    @NoAutoSerialisation
    public String getName() {
        return getReferencedContact().getName();
    }

    public Contact getReferencedContact() {
        return (Contact) getReferencedComponent();
    }

    public boolean isInput() {
        return getReferencedContact().isInput();
    }

    public boolean isOutput() {
        return getReferencedContact().isOutput();
    }

    public boolean isPort() {
        return getReferencedContact().isPort();
    }

    public boolean isDriver() {
        return getReferencedContact().isDriver();
    }

    public boolean isDriven() {
        return getReferencedContact().isDriven();
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
            getReferencedContact().setInitToOne(srcComponent.getReferencedContact().getInitToOne());
            // TODO: Note that IOType and Direction are currently NOT copied to allow input/output
            //       port generation with Shift key (and not to be copied from a template node).
            // getReferencedContact().setIOType(srcComponent.getReferencedContact().getIOType());
            // setDirection(srcComponent.getDirection());
        }
    }

}
