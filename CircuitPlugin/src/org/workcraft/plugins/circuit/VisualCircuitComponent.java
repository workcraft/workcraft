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
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.CustomTouchable;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@DisplayName("Abstract Component")
@Hotkey(KeyEvent.VK_A)
@SVGIcon("images/icons/svg/circuit-component.svg")
public class VisualCircuitComponent extends VisualComponent implements
        Container, CustomTouchable, StateObserver, ObservableHierarchy {
    public static final String PROPERTY_RENDER_TYPE = "Render type";

    private Color inputColor = VisualContact.inputColor;
    private Color outputColor = VisualContact.outputColor;

    double marginSize = 0.2;
    double contactLength = 0.5;
    double contactStep = 1.0;

    protected Rectangle2D internalBB = null;
    private WeakReference<VisualContact> mainContact = null;

    protected DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

    private HashMap<VisualContact, GlyphVector> contactLableGlyphs = new HashMap<>();

    public VisualCircuitComponent(CircuitComponent component) {
        super(component, true, true, true);
        component.addObserver(this);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualCircuitComponent, Boolean>(
                this, CircuitComponent.PROPERTY_IS_ENVIRONMENT, Boolean.class, true, true, true) {
            protected void setter(VisualCircuitComponent object, Boolean value) {
                object.setIsEnvironment(value);
            }

            protected Boolean getter(VisualCircuitComponent object) {
                return object.getIsEnvironment();
            }
        });
// TODO: Rename label to module name (?)
//        renamePropertyDeclarationByName(PROPERTY_LABEL, CircuitComponent.PROPERTY_MODULE);
    }

    public void setMainContact(VisualContact contact) {
        this.mainContact = new WeakReference<VisualContact>(contact);
    }

    public VisualContact getMainContact() {
        VisualContact ret = null;
        if (mainContact != null) {
            ret = mainContact.get();
        }
        if (ret == null) {
            for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
                if (vc.isOutput()) {
                    setMainContact(vc);
                    ret = vc;
                    break;
                }
            }
        }
        return ret;
    }

    public CircuitComponent getReferencedCircuitComponent() {
        return (CircuitComponent) this.getReferencedComponent();
    }

    public boolean getIsEnvironment() {
        if (getReferencedCircuitComponent() != null) {
            return getReferencedCircuitComponent().getIsEnvironment();
        }
        return false;
    }

    public void setIsEnvironment(boolean value) {
        if (getReferencedCircuitComponent() != null) {
            getReferencedCircuitComponent().setIsEnvironment(value);
        }
    }

    private LinkedList<VisualContact> getOrderedContacts(final Direction dir, final boolean reverse) {
        LinkedList<VisualContact> list = new LinkedList<>();
        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
            if (vc.getDirection() == dir) {
                list.add(vc);
            }
        }
        Collections.sort(list, new Comparator<VisualContact>() {
            @Override
            public int compare(VisualContact vc1, VisualContact vc2) {
                if ((dir == Direction.NORTH) || (dir == Direction.SOUTH)) {
                    return (reverse ? -1 : 1) * Double.compare(vc1.getX(), vc2.getX());
                } else {
                    return (reverse ? -1 : 1) * Double.compare(vc1.getY(), vc2.getY());
                }
            }
        });
        return list;
    }

    private int getContactCount(final Direction dir) {
        int count = 0;
        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
            if (vc.getDirection() == dir) {
                count++;
            }
        }
        return count;
    }

    private void spreadContactsEvenly() {
        int westCount = getContactCount(Direction.WEST);
        int northCount = getContactCount(Direction.NORTH);
        int eastCount = getContactCount(Direction.EAST);
        int southCount = getContactCount(Direction.SOUTH);

        double westPosition = -contactStep * (westCount - 1) / 2;
        double northPosition = -contactStep * (northCount - 1) / 2;
        double eastPosition = -contactStep * (eastCount - 1) / 2;
        double southPosition = -contactStep * (southCount - 1) / 2;
        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
            switch (vc.getDirection()) {
            case WEST:
                vc.setY(westPosition);
                westPosition += contactStep;
                break;
            case NORTH:
                vc.setX(northPosition);
                northPosition += contactStep;
                break;
            case EAST:
                vc.setY(eastPosition);
                eastPosition += contactStep;
                break;
            case SOUTH:
                vc.setX(southPosition);
                southPosition += contactStep;
                break;
            }
        }
        invalidateBoundingBox();
    }

    public Collection<VisualContact> getContacts() {
        return Hierarchy.getChildrenOfType(this, VisualContact.class);
    }

    public Collection<VisualConnection> getRelevantConnections(Collection<VisualContact> contacts) {
        Collection<VisualConnection> result = Collections.emptyList();
        Node root = Hierarchy.getRoot(this);
        if (root != null) {
            final HashSet<VisualContact> contactsHashSet = new HashSet<>(contacts);
            result = Hierarchy.getDescendantsOfType(root, VisualConnection.class,
                new Func<VisualConnection, Boolean>() {
                    @Override
                    public Boolean eval(VisualConnection arg) {
                        return contactsHashSet.contains(arg.getFirst()) || contactsHashSet.contains(arg.getSecond());
                    }
                });
        }
        return result;
    }

    public void setContactsDefaultPosition() {
        spreadContactsEvenly();

        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();

        Collection<VisualContact> contacts = getContacts();
        for (VisualContact vc: contacts) {
            switch (vc.getDirection()) {
            case WEST:
                vc.setX(bb.getMinX() - contactLength);
                break;
            case NORTH:
                vc.setY(bb.getMinY() - contactLength);
                break;
            case EAST:
                vc.setX(bb.getMaxX() + contactLength);
                break;
            case SOUTH:
                vc.setY(bb.getMaxY() + contactLength);
                break;
            }
        }
        invalidateBoundingBox();
    }

    public void centerPivotPoint() {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        setX(getX() + bb.getCenterX());
        setY(getY() + bb.getCenterY());
        Collection<VisualContact> contacts = getContacts();
        for (VisualContact vc: contacts) {
            vc.setX(vc.getX() - bb.getCenterX());
            vc.setY(vc.getY() - bb.getCenterY());
        }
        invalidateBoundingBox();
    }

    public void addContact(VisualCircuit vcircuit, VisualContact vc) {
        if (!getChildren().contains(vc)) {
            LinkedList<VisualContact> sameSideContacts = getOrderedContacts(vc.getDirection(), true);
            Container container = NamespaceHelper.getMathContainer(vcircuit, this);
            container.add(vc.getReferencedComponent());
            add(vc);

            Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
            switch (vc.getDirection()) {
            case WEST:
                vc.setX(TransformHelper.snapP5(bb.getMinX() - contactLength));
                if (sameSideContacts.size() > 0) {
                    vc.setY(sameSideContacts.getFirst().getY() + contactLength);
                }
                break;
            case NORTH:
                vc.setY(TransformHelper.snapP5(bb.getMinY() - contactLength));
                if (sameSideContacts.size() > 0) {
                    vc.setX(sameSideContacts.getFirst().getX() + contactLength);
                }
                break;
            case EAST:
                vc.setX(TransformHelper.snapP5(bb.getMaxX() + contactLength));
                if (sameSideContacts.size() > 0) {
                    vc.setY(sameSideContacts.getFirst().getY() + contactLength);
                }
                break;
            case SOUTH:
                vc.setY(TransformHelper.snapP5(bb.getMaxY() + contactLength));
                if (sameSideContacts.size() > 0) {
                    vc.setX(sameSideContacts.getFirst().getX() + contactLength);
                }
                break;
            }
            invalidateBoundingBox();
        }
    }

    public void invalidateBoundingBox() {
        internalBB = null;
    }

    private Rectangle2D getContactMinimalBox() {
        double x1 = -size / 2;
        double y1 = -size / 2;
        double x2 = size / 2;
        double y2 = size / 2;
        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
            switch (vc.getDirection()) {
            case WEST:
                double westX = vc.getX() + contactLength;
                if ((westX < -size / 2) && (westX > x1)) {
                    x1 = westX;
                }
                break;
            case NORTH:
                double northY = vc.getY() + contactLength;
                if ((northY < -size / 2) && (northY > y1)) {
                    y1 = northY;
                }
                break;
            case EAST:
                double eastX = vc.getX() - contactLength;
                if ((eastX > size / 2) && (eastX < x2)) {
                    x2 = eastX;
                }
                break;
            case SOUTH:
                double southY = vc.getY() - contactLength;
                if ((southY > size / 2) && (southY < y2)) {
                    y2 = southY;
                }
                break;
            }
        }
        return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    private Rectangle2D getContactExpandedBox() {
        Rectangle2D minBox = getContactMinimalBox();
        double x1 = minBox.getMinX();
        double y1 = minBox.getMinY();
        double x2 = minBox.getMaxX();
        double y2 = minBox.getMaxY();
        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
            double x = vc.getX();
            double y = vc.getY();
            switch (vc.getDirection()) {
            case WEST:
                if (vc.getX() < minBox.getMinX()) {
                    y1 = Math.min(y1, y - contactStep / 2);
                    y2 = Math.max(y2, y + contactStep / 2);
                }
                break;
            case NORTH:
                if (vc.getY() < minBox.getMinY()) {
                    x1 = Math.min(x1, x - contactStep / 2);
                    x2 = Math.max(x2, x + contactStep / 2);
                }
                break;
            case EAST:
                if (vc.getX() > minBox.getMaxX()) {
                    y1 = Math.min(y1, y - contactStep / 2);
                    y2 = Math.max(y2, y + contactStep / 2);
                }
                break;
            case SOUTH:
                if (vc.getY() > minBox.getMaxY()) {
                    x1 = Math.min(x1, x - contactStep / 2);
                    x2 = Math.max(x2, x + contactStep / 2);
                }
                break;
            }
        }
        return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    private Rectangle2D getContactBestBox() {
        Rectangle2D expBox = getContactExpandedBox();
        double x1 = expBox.getMinX();
        double y1 = expBox.getMinY();
        double x2 = expBox.getMaxX();
        double y2 = expBox.getMaxY();

        boolean westFirst = true;
        boolean northFirst = true;
        boolean eastFirst = true;
        boolean southFirst = true;

        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
            double x = vc.getX();
            double y = vc.getY();
            switch (vc.getDirection()) {
            case WEST:
                if (westFirst) {
                    x1 = x + contactLength;
                } else {
                    x1 = Math.max(x1, x + contactLength);
                }
                westFirst = false;
                break;
            case NORTH:
                if (northFirst) {
                    y1 = y + contactLength;
                } else {
                    y1 = Math.max(y1, y + contactLength);
                }
                northFirst = false;
                break;
            case EAST:
                if (eastFirst) {
                    x2 = x - contactLength;
                } else {
                    x2 = Math.min(x2, x - contactLength);
                }
                eastFirst = false;
                break;
            case SOUTH:
                if (southFirst) {
                    y2 = y - contactLength;
                } else {
                    y2 = Math.min(y2, y - contactLength);
                }
                southFirst = false;
                break;
            }
        }

        if (x1 > x2) {
            x1 = x2 = (x1 + x2) / 2;
        }
        if (y1 > y2) {
            y1 = y2 = (y1 + y2) / 2;
        }
        Rectangle2D maxBox = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
        return BoundingBoxHelper.union(expBox, maxBox);
    }

    private Point2D getContactLinePosition(VisualContact vc) {
        Point2D result = null;
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        switch (vc.getDirection()) {
        case NORTH:
            result = new Point2D.Double(vc.getX(), bb.getMinY());
            break;
        case EAST:
            result = new Point2D.Double(bb.getMaxX(), vc.getY());
            break;
        case SOUTH:
            result = new Point2D.Double(vc.getX(), bb.getMaxY());
            break;
        case WEST:
            result = new Point2D.Double(bb.getMinX(), vc.getY());
            break;
        }
        return result;
    }

    private void drawContactLines(DrawRequest r) {
        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
            Point2D p1 = vc.getPosition();
            Point2D p2 = getContactLinePosition(vc);
            if ((p1 != null) && (p2 != null)) {
                Graphics2D g = r.getGraphics();
                Decoration d = r.getDecoration();
                Color colorisation = d.getColorisation();
                g.setStroke(new BasicStroke((float) CircuitSettings.getWireWidth()));
                g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
                Line2D line = new Line2D.Double(p1, p2);
                g.draw(line);
            }
        }
    }

    private GlyphVector getContactLabelGlyphs(DrawRequest r, VisualContact vc) {
        Circuit circuit = (Circuit) r.getModel().getMathModel();
        String name = circuit.getName(vc.getReferencedContact());
        final FontRenderContext context = new FontRenderContext(AffineTransform.getScaleInstance(1000.0, 1000.0), true, true);
        GlyphVector gv = contactLableGlyphs.get(vc);
        if (gv == null) {
            gv = nameFont.createGlyphVector(context, name);
            contactLableGlyphs.put(vc, gv);
        }
        return gv;
    }

    private void drawContactLabel(DrawRequest r, VisualContact vc) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        Color colorisation = d.getColorisation();
        Color color = vc.isInput() ? inputColor : outputColor;
        g.setColor(Coloriser.colorise(color, colorisation));

        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        GlyphVector gv = getContactLabelGlyphs(r, vc);
        Rectangle2D labelBB = gv.getVisualBounds();

        float labelX = 0.0f;
        float labelY = 0.0f;
        switch (vc.getDirection()) {
        case NORTH:
            labelX = (float) (-bb.getMinY() - marginSize - labelBB.getWidth());
            labelY = (float) (vc.getX() + labelBB.getHeight() / 2);
            break;
        case EAST:
            labelX = (float) (bb.getMaxX() - marginSize - labelBB.getWidth());
            labelY = (float) (vc.getY() + labelBB.getHeight() / 2);
            break;
        case SOUTH:
            labelX = (float) (-bb.getMaxY() + marginSize);
            labelY = (float) (vc.getX() + labelBB.getHeight() / 2);
            break;
        case WEST:
            labelX = (float) (bb.getMinX() + marginSize);
            labelY = (float) (vc.getY() + labelBB.getHeight() / 2);
            break;
        }
        g.drawGlyphVector(gv, labelX, labelY);
    }

    protected void drawContactLabels(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        AffineTransform savedTransform = g.getTransform();

        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class,
                new Func<VisualContact, Boolean>() {
                    @Override
                    public Boolean eval(VisualContact arg) {
                        return (arg.getDirection() == Direction.WEST) || (arg.getDirection() == Direction.EAST);
                    }
                })) {
            drawContactLabel(r, vc);
        }

        AffineTransform rotateTransform = new AffineTransform();
        rotateTransform.quadrantRotate(-1);
        g.transform(rotateTransform);

        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class,
                new Func<VisualContact, Boolean>() {
                    @Override
                    public Boolean eval(VisualContact arg) {
                        return (arg.getDirection() == Direction.NORTH) || (arg.getDirection() == Direction.SOUTH);
                    }
                })) {
            drawContactLabel(r, vc);
        }

        g.setTransform(savedTransform);
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        if ((groupImpl != null) && (internalBB == null)) {
            internalBB = getContactBestBox();
        }
        Rectangle2D bb = BoundingBoxHelper.copy(internalBB);
        if (bb == null) {
            bb = super.getInternalBoundingBoxInLocalSpace();
        }
        return bb;
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        Collection<Touchable> touchableChildren = Hierarchy.getChildrenOfType(this, Touchable.class);
        Rectangle2D childrenBB = BoundingBoxHelper.mergeBoundingBoxes(touchableChildren);
        return BoundingBoxHelper.union(bb, childrenBB);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        // Cache rendered text to better estimate the bounding box
        cacheRenderedText(r);

        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();

        g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
        g.fill(bb);
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        if (getIsEnvironment()) {
            float[] dash = {0.25f, 0.25f };
            g.setStroke(new BasicStroke((float) CircuitSettings.getBorderWidth(),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f));
        } else {
            g.setStroke(new BasicStroke((float) CircuitSettings.getBorderWidth()));
        }

        g.draw(bb);

        if (d.getColorisation() != null) {
            drawPivot(r);
        }

        drawContactLines(r);
        drawContactLabels(r);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public void add(Node node) {
        groupImpl.add(node);
        if (node instanceof VisualContact) {
            ((VisualContact) node).addObserver(this);
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
        if (node instanceof VisualContact) {
            invalidateBoundingBox();
            contactLableGlyphs.remove(node);
        }
        groupImpl.remove(node);
    }

    @Override
    public void add(Collection<Node> nodes) {
        groupImpl.add(nodes);
        for (Node node : nodes) {
            if (node instanceof VisualContact) {
                ((VisualContact) node).addObserver(this);
            }
        }
    }

    @Override
    public void remove(Collection<Node> nodes) {
        for (Node n : nodes) {
            remove(n);
        }
    }

    @Override
    public void reparent(Collection<Node> nodes, Container newParent) {
        groupImpl.reparent(nodes, newParent);
    }

    @Override
    public void reparent(Collection<Node> nodes) {
        groupImpl.reparent(nodes);
    }

    @Override
    public Node customHitTest(Point2D point) {
        Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
        for (Node node : getChildren()) {
            if (node instanceof VisualNode) {
                VisualNode vn = (VisualNode) node;
                if (vn.hitTest(pointInLocalSpace)) {
                    return vn;
                }
            }
        }
        return hitTest(point) ? this : null;
    }

    @Override
    public void notify(StateEvent e) {
        if (e instanceof TransformChangedEvent) {
            TransformChangedEvent t = (TransformChangedEvent) e;
            if (t.sender instanceof VisualContact) {
                VisualContact vc = (VisualContact) t.sender;

                AffineTransform at = t.sender.getTransform();
                double x = at.getTranslateX();
                double y = at.getTranslateY();
                Rectangle2D bb = getContactExpandedBox(); //getContactMinimalBox(); //getInternalBoundingBoxInLocalSpace();
                if ((x <= bb.getMinX()) && (y > bb.getMinY()) && (y < bb.getMaxY())) {
                    vc.setDirection(Direction.WEST);
                }
                if ((x >= bb.getMaxX()) && (y > bb.getMinY()) && (y < bb.getMaxY())) {
                    vc.setDirection(Direction.EAST);
                }
                if ((y <= bb.getMinY()) && (x > bb.getMinX()) && (x < bb.getMaxX())) {
                    vc.setDirection(Direction.NORTH);
                }
                if ((y >= bb.getMaxY()) && (x > bb.getMinX()) && (x < bb.getMaxX())) {
                    vc.setDirection(Direction.SOUTH);
                }
                invalidateBoundingBox();
            }
        }

        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pc = (PropertyChangedEvent) e;
            String propertyName = pc.getPropertyName();
            if (propertyName.equals(Contact.PROPERTY_NAME)
                    || propertyName.equals(Contact.PROPERTY_IO_TYPE)
                    || propertyName.equals(VisualContact.PROPERTY_DIRECTION)) {

                invalidateBoundingBox();
                contactLableGlyphs.clear();
            }
        }
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
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualCircuitComponent) {
            VisualCircuitComponent srcComponent = (VisualCircuitComponent) src;
            setIsEnvironment(srcComponent.getIsEnvironment());
        }
    }

    @Override
    public String getLabel() {
        return getReferencedCircuitComponent().getModule();
    }

    @Override
    public void setLabel(String label) {
        getReferencedCircuitComponent().setModule(label);
        super.setLabel(label);
    }

}
