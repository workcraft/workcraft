package org.workcraft.plugins.xmas.components;

import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.*;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.XmasContact.IOType;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

public abstract class VisualXmasComponent extends VisualComponent implements Container, StateObserver, ObservableHierarchy {

    public static final String DEGREE_SYMBOL = Character.toString((char) 0x00B0);
    public static final double SIZE = VisualCommonSettings.getNodeSize();
    public static final double TOKEN_SIZE = 0.18 * SIZE;

    private static final String PROPERTY_ORIENTATION = "Orientation";

    public enum Orientation {
        ORIENTATION_0("0" + DEGREE_SYMBOL, 0),
        ORIENTATION_90("90" + DEGREE_SYMBOL, 1),
        ORIENTATION_180("180" + DEGREE_SYMBOL, 2),
        ORIENTATION_270("270" + DEGREE_SYMBOL, 3);

        private final String name;
        private final int quadrant;

        Orientation(String name, int quadrant) {
            this.name = name;
            this.quadrant = quadrant;
        }

        public int getQuadrant() {
            return quadrant;
        }

        @Override
        public String toString() {
            return name;
        }

        public Orientation rotateClockwise() {
            return switch (this) {
                case ORIENTATION_0 -> ORIENTATION_90;
                case ORIENTATION_90 -> ORIENTATION_180;
                case ORIENTATION_180 -> ORIENTATION_270;
                case ORIENTATION_270 -> ORIENTATION_0;
            };
        }

        public Orientation rotateCounterclockwise() {
            return switch (this) {
                case ORIENTATION_0 -> ORIENTATION_270;
                case ORIENTATION_90 -> ORIENTATION_0;
                case ORIENTATION_180 -> ORIENTATION_90;
                case ORIENTATION_270 -> ORIENTATION_180;
            };
        }

        public Orientation flipHorizontal() {
            return switch (this) {
                case ORIENTATION_0 -> ORIENTATION_180;
                case ORIENTATION_180 -> ORIENTATION_0;
                default -> this;
            };
        }

        public Orientation flipVertical() {
            return switch (this) {
                case ORIENTATION_90 -> ORIENTATION_270;
                case ORIENTATION_270 -> ORIENTATION_90;
                default -> this;
            };
        }

    }

    private Orientation orientation = Orientation.ORIENTATION_0;
    protected DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

    public VisualXmasComponent(XmasComponent component) {
        super(component);
        component.addObserver(this);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Orientation.class, PROPERTY_ORIENTATION,
                this::setOrientation, this::getOrientation).setCombinable().setTemplatable());
    }

    @Override
    public XmasComponent getReferencedComponent() {
        return (XmasComponent) super.getReferencedComponent();
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation value) {
        if (orientation != value) {
            for (VisualXmasContact contact: getContacts()) {
                AffineTransform rotateTransform = new AffineTransform();
                rotateTransform.quadrantRotate(value.getQuadrant() - getOrientation().getQuadrant());
                TransformHelper.applyTransform(contact, rotateTransform);
            }
            orientation = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_ORIENTATION));
        }
    }

    public Collection<VisualXmasContact> getContacts() {
        ArrayList<VisualXmasContact> result = new ArrayList<>();
        for (Node n: getChildren()) {
            if (n instanceof VisualXmasContact) {
                result.add((VisualXmasContact) n);
            }
        }
        return result;
    }

    public void addContact(VisualXmasContact vc) {
        if (!getChildren().contains(vc)) {
            getReferencedComponent().add(vc.getReferencedComponent());
            add(vc);
        }
    }

    public void setContactPosition(VisualXmasContact vc, Positioning positioning) {
        double size2 = VisualCommonSettings.getNodeSize() / 2.0;
        vc.setPosition(new Point2D.Double(size2 * positioning.xSign, size2 * positioning.ySign));
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return transformShape(getShape()).getBounds2D();
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        for (VisualXmasContact c: getContacts()) {
            Rectangle2D.union(bb, c.getBoundingBox(), bb);
        }
        return bb;
    }

    @Override
    public void add(Node node) {
        groupImpl.add(node);
        if (node instanceof VisualXmasContact) {
            ((VisualXmasContact) node).addObserver(this);
        }
    }

    @Override
    public Collection<Node> getChildren() {
        return groupImpl.getChildren();
    }

    @Override
    public Node getParent() {
        return groupImpl.getParent();
    }

    @Override
    public void setParent(Node parent) {
        groupImpl.setParent(parent);
    }

    @Override
    public void remove(Node node) {
    }

    @Override
    public void add(Collection<? extends Node> nodes) {
        for (Node x : nodes) {
            add(x);
        }
    }

    @Override
    public void remove(Collection<? extends Node> nodes) {
    }

    @Override
    public void reparent(Collection<? extends Node> nodes, Container newParent) {
        groupImpl.reparent(nodes, newParent);
    }

    @Override
    public void reparent(Collection<? extends Node> nodes) {
        groupImpl.reparent(nodes);
    }

    @Override
    public void notify(StateEvent e) {
    }

    public VisualXmasContact createInput(Positioning positioning) {
        VisualXmasContact contact = new VisualXmasContact(new XmasContact(IOType.INPUT));
        addContact(contact);
        setContactPosition(contact, positioning);
        return contact;
    }

    public VisualXmasContact createOutput(Positioning positioning) {
        VisualXmasContact contact = new VisualXmasContact(new XmasContact(IOType.OUTPUT));
        addContact(contact);
        setContactPosition(contact, positioning);
        return contact;
    }

    @Override
    public void addObserver(HierarchyObserver obs) {
        groupImpl.addObserver(obs);
    }

    @Override
    public void removeObserver(HierarchyObserver obs) {
        groupImpl.removeObserver(obs);
    }

    @Override
    public void removeAllObservers() {
        groupImpl.removeAllObservers();
    }

    @Override
    public abstract Shape getShape();

    public Shape transformShape(Shape shape) {
        AffineTransform rotateTransform = new AffineTransform();
        if (orientation != null) {
            rotateTransform.quadrantRotate(orientation.getQuadrant());
        }
        return rotateTransform.createTransformedShape(shape);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        g.setStroke(new BasicStroke((float) XmasSettings.getBorderWidth()));
        g.draw(transformShape(getShape()));

        drawNameInLocalSpace(r);
        drawLabelInLocalSpace(r);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualXmasComponent srcComponent) {
            setOrientation(srcComponent.getOrientation());
        }
    }

    @Override
    public void rotateClockwise() {
        setOrientation(getOrientation().rotateClockwise());
        super.rotateClockwise();
    }

    @Override
    public void rotateCounterclockwise() {
        setOrientation(getOrientation().rotateCounterclockwise());
        super.rotateCounterclockwise();
    }

    @Override
    public void flipHorizontal() {
        setOrientation(getOrientation().flipHorizontal());
        super.flipHorizontal();
    }

    @Override
    public void flipVertical() {
        setOrientation(getOrientation().flipVertical());
        super.flipVertical();
    }

}
