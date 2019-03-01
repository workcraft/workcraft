package org.workcraft.plugins.xmas.components;

import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.observation.*;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.XmasContact.IOType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

public abstract class VisualXmasComponent extends VisualComponent implements Container, StateObserver, ObservableHierarchy {
    // Degree symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final char DEGREE_SYMBOL = 0x00B0;
    public static final double SIZE = CommonVisualSettings.getNodeSize();
    public static final double TOKEN_SIZE = 0.18 * SIZE;

    private static final String PROPERTY_ORIENTATION = "Orientation";

    public enum Orientation {
        ORIENTATION_0("0" + Character.toString(DEGREE_SYMBOL), 0),
        ORIENTATION_90("90" + Character.toString(DEGREE_SYMBOL), 1),
        ORIENTATION_180("180" + Character.toString(DEGREE_SYMBOL), 2),
        ORIENTATION_270("270" + Character.toString(DEGREE_SYMBOL), 3);

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
            switch (this) {
            case ORIENTATION_0: return ORIENTATION_90;
            case ORIENTATION_90: return ORIENTATION_180;
            case ORIENTATION_180: return ORIENTATION_270;
            case ORIENTATION_270: return ORIENTATION_0;
            default: return this;
            }
        }

        public Orientation rotateCounterclockwise() {
            switch (this) {
            case ORIENTATION_0: return ORIENTATION_270;
            case ORIENTATION_90: return ORIENTATION_0;
            case ORIENTATION_180: return ORIENTATION_90;
            case ORIENTATION_270: return ORIENTATION_180;
            default: return this;
            }
        }

        public Orientation flipHorizontal() {
            switch (this) {
            case ORIENTATION_0: return ORIENTATION_180;
            case ORIENTATION_180: return ORIENTATION_0;
            default: return this;
            }
        }

        public Orientation flipVertical() {
            switch (this) {
            case ORIENTATION_90: return ORIENTATION_270;
            case ORIENTATION_270: return ORIENTATION_90;
            default: return this;
            }
        }

    };

    private Orientation orientation = Orientation.ORIENTATION_0;
    protected DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

    public VisualXmasComponent(XmasComponent component) {
        super(component);
        component.addObserver(this);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualXmasComponent, Orientation>(
                this, PROPERTY_ORIENTATION, Orientation.class, true, true) {
            @Override
            public void setter(VisualXmasComponent object, Orientation value) {
                object.setOrientation(value);
            }
            @Override
            public Orientation getter(VisualXmasComponent object) {
                return object.getOrientation();
            }
        });
    }

    public XmasComponent getReferencedXmasComponent() {
        return (XmasComponent) getReferencedComponent();
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

    public void addContact(VisualXmasContact vc, Positioning positioning) {
        if (!getChildren().contains(vc)) {
            getReferencedXmasComponent().add(vc.getReferencedComponent());
            add(vc);
            setContactPosition(vc, positioning);
        }
    }

    public void setContactPosition(VisualXmasContact vc, Positioning positioning) {
        double size2 = CommonVisualSettings.getNodeSize() / 2.0;
        vc.setPosition(new Point2D.Double(size2 * positioning.xSign, size2 * positioning.ySign));
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return transformShape(getShape()).getBounds2D();
    }

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
    public void remove(Node node) { }

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

    public VisualXmasContact addInput(Positioning positioning) {
        XmasContact c = new XmasContact(IOType.INPUT);
        VisualXmasContact vc = new VisualXmasContact(c);
        addContact(vc, positioning);
        return vc;
    }

    public VisualXmasContact addOutput(Positioning positioning) {
        XmasContact c = new XmasContact(IOType.OUTPUT);
        VisualXmasContact vc = new VisualXmasContact(c);
        addContact(vc, positioning);
        return vc;
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

        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        g.setStroke(new BasicStroke((float) XmasSettings.getBorderWidth()));
        g.draw(transformShape(getShape()));

        drawNameInLocalSpace(r);
        drawLabelInLocalSpace(r);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualXmasComponent) {
            VisualXmasComponent srcComponent = (VisualXmasComponent) src;
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
