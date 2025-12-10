package org.workcraft.plugins.circuit;

import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.*;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VisualCircuitComponent extends VisualComponent
        implements Container, CustomTouchable, StateObserver, ObservableHierarchy {

    private static final AffineTransform TRANSFORM = AffineTransform.getScaleInstance(1000.0, 1000.0);
    private static final FontRenderContext CONTEXT = new FontRenderContext(TRANSFORM, true, true);

    public static final String PROPERTY_RENDER_TYPE = "Render type";

    private static final double labelMargin = 0.2;
    private static final double contactLength = 0.5;
    private static final double contactMinOffset = 2.0;
    private static final double contactStep = 1.0;
    private static final double contactMargin = 0.5;

    public Rectangle2D internalBB = null;
    public DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

    private final HashMap<VisualContact, GlyphVector> contactNameGlyphs = new HashMap<>();
    private static double contactFontSize = CircuitSettings.getContactFontSize();

    public VisualCircuitComponent(CircuitComponent component) {
        super(component, true, true, true);
        component.addObserver(this);
        addPropertyDeclarations();
    }

    @SuppressWarnings("EmptyMethod")
    private void addPropertyDeclarations() {
        // TODO: Rename label to module name (?)
        //renamePropertyDeclarationByName(PROPERTY_LABEL, "Module name");
        //renamePropertyDeclarationByName(PROPERTY_LABEL_COLOR, "Module name color");
        //renamePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING, "Module name positioning");
    }

    @Override
    public CircuitComponent getReferencedComponent() {
        return (CircuitComponent) super.getReferencedComponent();
    }

    @NoAutoSerialisation
    public boolean getIsEnvironment() {
        if (getReferencedComponent() != null) {
            return getReferencedComponent().getIsEnvironment();
        }
        return false;
    }

    @NoAutoSerialisation
    public void setIsEnvironment(boolean value) {
        if (getReferencedComponent() != null) {
            getReferencedComponent().setIsEnvironment(value);
        }
    }

    public boolean isMapped() {
        return (getReferencedComponent() != null) && getReferencedComponent().isMapped();
    }

    public void clearMapping() {
        getReferencedComponent().setModule("");
    }

    private LinkedList<VisualContact> getOrderedOutsideContacts(Direction dir) {
        LinkedList<VisualContact> list = new LinkedList<>();
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        for (VisualContact vc : Hierarchy.getChildrenOfType(this, VisualContact.class)) {
            if ((vc.getDirection() == dir) && !bb.contains(vc.getX(), vc.getY())) {
                list.add(vc);
            }
        }
        list.sort((vc1, vc2) -> {
            if ((dir == Direction.NORTH) || (dir == Direction.SOUTH)) {
                return Double.compare(vc1.getX(), vc2.getX());
            } else {
                return Double.compare(vc1.getY(), vc2.getY());
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
        for (VisualContact contact : Hierarchy.getChildrenOfType(this, VisualContact.class)) {
            switch (contact.getDirection()) {
                case WEST -> {
                    contact.setY(westPosition);
                    westPosition += contactStep;
                }
                case NORTH -> {
                    contact.setX(northPosition);
                    northPosition += contactStep;
                }
                case EAST -> {
                    contact.setY(eastPosition);
                    eastPosition += contactStep;
                }
                case SOUTH -> {
                    contact.setX(southPosition);
                    southPosition += contactStep;
                }
            }
        }
        invalidateBoundingBox();
    }

    public void setContactsDefaultPosition() {
        spreadContactsEvenly();

        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();

        Collection<VisualContact> contacts = getVisualContacts();
        for (VisualContact contact : contacts) {
            switch (contact.getDirection()) {
                case WEST -> contact.setX(bb.getMinX() - contactLength);
                case NORTH -> contact.setY(bb.getMinY() - contactLength);
                case EAST -> contact.setX(bb.getMaxX() + contactLength);
                case SOUTH -> contact.setY(bb.getMaxY() + contactLength);
            }
        }
        invalidateBoundingBox();
    }

    public void repackContactsPosition() {
        Collection<VisualContact> contacts = getVisualContacts();
        double westStart = 0.0;
        double northStart = 0.0;
        double eastStart = 0.0;
        double southStart = 0.0;
        for (VisualContact contact : contacts) {
            switch (contact.getDirection()) {
                case NORTH, SOUTH -> {
                    eastStart = Math.max(eastStart, contact.getX());
                    westStart = Math.min(westStart, contact.getX());
                }
                case WEST, EAST -> {
                    northStart = Math.min(northStart, contact.getY());
                    southStart = Math.max(southStart, contact.getY());
                }
            }
        }
        double westPosition = -contactMinOffset;
        double northPosition = -contactMinOffset;
        double eastPosition = contactMinOffset;
        double southPosition = contactMinOffset;
        for (VisualContact contact : contacts) {
            GlyphVector gv = getContactNameGlyphs(contact, contact::getName);
            double textWidth = gv.getVisualBounds().getWidth();
            double contactBestPosition = textWidth + contactMargin + contactLength;
            switch (contact.getDirection()) {
                case WEST -> westPosition = Math.min(westPosition, -contactBestPosition);
                case NORTH -> northPosition = Math.min(northPosition, -contactBestPosition);
                case EAST -> eastPosition = Math.max(eastPosition, contactBestPosition);
                case SOUTH -> southPosition = Math.max(southPosition, contactBestPosition);
            }
        }
        for (VisualContact contact : contacts) {
            switch (contact.getDirection()) {
                case WEST -> contact.setX(TransformHelper.snapP5(westStart + westPosition));
                case NORTH -> contact.setY(TransformHelper.snapP5(northStart + northPosition));
                case EAST -> contact.setX(TransformHelper.snapP5(eastStart + eastPosition));
                case SOUTH -> contact.setY(TransformHelper.snapP5(southStart + southPosition));
            }
        }
        invalidateBoundingBox();
    }

    @Override
    public void centerPivotPoint(boolean horizontal, boolean vertical) {
        super.centerPivotPoint(horizontal, vertical);
        invalidateBoundingBox();
    }

    public VisualContact createContact(Contact.IOType ioType) {
        VisualContact vc = new VisualContact(new Contact(ioType));
        addContact(vc);
        return vc;
    }

    public void addContact(VisualContact vc) {
        if (!getChildren().contains(vc)) {
            getReferencedComponent().add(vc.getReferencedComponent());
            add(vc);
        }
    }

    public void setPositionByDirection(VisualContact contact, Direction direction, boolean reverseProgression) {
        contact.setDirection(direction);
        Collection<VisualContact> contacts = Hierarchy.getChildrenOfType(this, VisualContact.class);
        contacts.remove(contact);
        double contactOffset = getContactBestOffset(contacts, direction);
        switch (direction) {
            case WEST, EAST -> {
                contact.setX(contactOffset);
                positionVertical(contact, reverseProgression);
            }
            case NORTH, SOUTH -> {
                contact.setY(contactOffset);
                positionHorizontal(contact, reverseProgression);
            }
        }
        invalidateBoundingBox();
    }

    private double getContactBestOffset(Collection<VisualContact> contacts, Direction direction) {
        Rectangle2D bb = getContactExpandedBox(contacts);
        contacts = contacts.stream()
                .filter(contact -> contact.getDirection() == direction)
                .collect(Collectors.toSet());

        switch (direction) {
            case WEST -> {
                double westOffset = contacts.stream().mapToDouble(VisualContact::getX).max().orElse(-contactMinOffset);
                return Math.min(TransformHelper.snapP5(bb.getMinX() - contactLength), westOffset);
            }
            case NORTH -> {
                double northOffset = contacts.stream().mapToDouble(VisualContact::getY).max().orElse(-contactMinOffset);
                return Math.min(TransformHelper.snapP5(bb.getMinY() - contactLength), northOffset);
            }
            case EAST -> {
                double eastOffset = contacts.stream().mapToDouble(VisualContact::getX).min().orElse(contactMinOffset);
                return Math.max(TransformHelper.snapP5(bb.getMaxX() + contactLength), eastOffset);
            }
            case SOUTH -> {
                double southOffset = contacts.stream().mapToDouble(VisualContact::getY).min().orElse(contactMinOffset);
                return Math.max(TransformHelper.snapP5(bb.getMaxY() + contactLength), southOffset);
            }
        }
        return 0.0;
    }

    private void positionHorizontal(VisualContact vc, boolean reverseProgression) {
        LinkedList<VisualContact> contacts = getOrderedOutsideContacts(vc.getDirection());
        contacts.remove(vc);
        double x = 0.0;
        if (!contacts.isEmpty()) {
            if (reverseProgression) {
                x = TransformHelper.snapP5(contacts.getFirst().getX() - contactStep);
                for (VisualContact contact : getOrderedOutsideContacts(Direction.WEST)) {
                    if (contact.getX() > x - contactMargin - contactLength) {
                        contact.setX(x - contactMargin - contactLength);
                    }
                }
            } else {
                x = TransformHelper.snapP5(contacts.getLast().getX() + contactStep);
                for (VisualContact contact : getOrderedOutsideContacts(Direction.EAST)) {
                    if (contact.getX() < x + contactMargin + contactLength) {
                        contact.setX(x + contactMargin + contactLength);
                    }
                }
            }
        }
        vc.setX(x);
    }

    private void positionVertical(VisualContact vc, boolean reverseProgression) {
        LinkedList<VisualContact> contacts = getOrderedOutsideContacts(vc.getDirection());
        contacts.remove(vc);
        double y = 0.0;
        if (!contacts.isEmpty()) {
            if (reverseProgression) {
                y = TransformHelper.snapP5(contacts.getFirst().getY() - contactStep);
                for (VisualContact contact : getOrderedOutsideContacts(Direction.NORTH)) {
                    if (contact.getY() > y - contactMargin - contactLength) {
                        contact.setY(y - contactMargin - contactLength);
                    }
                }
            } else {
                y = TransformHelper.snapP5(contacts.getLast().getY() + contactStep);
                for (VisualContact contact : getOrderedOutsideContacts(Direction.SOUTH)) {
                    if (contact.getY() < y + contactMargin + contactLength) {
                        contact.setY(y + contactMargin + contactLength);
                    }
                }
            }
        }
        vc.setY(y);
    }

    public void invalidateBoundingBox() {
        internalBB = null;
    }

    private Rectangle2D getContactMinimalBox(Collection<VisualContact> contacts) {
        double size = VisualCommonSettings.getNodeSize();
        double xMin = -size / 2;
        double yMin = -size / 2;
        double xMax = size / 2;
        double yMax = size / 2;
        for (VisualContact contact : contacts) {
            switch (contact.getDirection()) {
                case WEST -> {
                    double xWest = contact.getX() + contactLength;
                    if ((xWest < -size / 2) && (xWest > xMin)) {
                        xMin = xWest;
                    }
                }
                case NORTH -> {
                    double yNorth = contact.getY() + contactLength;
                    if ((yNorth < -size / 2) && (yNorth > yMin)) {
                        yMin = yNorth;
                    }
                }
                case EAST -> {
                    double xEast = contact.getX() - contactLength;
                    if ((xEast > size / 2) && (xEast < xMax)) {
                        xMax = xEast;
                    }
                }
                case SOUTH -> {
                    double ySouth = contact.getY() - contactLength;
                    if ((ySouth > size / 2) && (ySouth < yMax)) {
                        yMax = ySouth;
                    }
                }
            }
        }
        return new Rectangle2D.Double(xMin, yMin, xMax - xMin, yMax - yMin);
    }

    private Rectangle2D getContactExpandedBox(Collection<VisualContact> contacts) {
        Rectangle2D minBox = getContactMinimalBox(contacts);
        double x1 = minBox.getMinX();
        double y1 = minBox.getMinY();
        double x2 = minBox.getMaxX();
        double y2 = minBox.getMaxY();
        for (VisualContact contact : contacts) {
            double x = contact.getX();
            double y = contact.getY();
            switch (contact.getDirection()) {
                case WEST -> {
                    if (contact.getX() < minBox.getMinX()) {
                        y1 = Math.min(y1, y - contactMargin);
                        y2 = Math.max(y2, y + contactMargin);
                    }
                }
                case NORTH -> {
                    if (contact.getY() < minBox.getMinY()) {
                        x1 = Math.min(x1, x - contactMargin);
                        x2 = Math.max(x2, x + contactMargin);
                    }
                }
                case EAST -> {
                    if (contact.getX() > minBox.getMaxX()) {
                        y1 = Math.min(y1, y - contactMargin);
                        y2 = Math.max(y2, y + contactMargin);
                    }
                }
                case SOUTH -> {
                    if (contact.getY() > minBox.getMaxY()) {
                        x1 = Math.min(x1, x - contactMargin);
                        x2 = Math.max(x2, x + contactMargin);
                    }
                }
            }
        }
        return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    @SuppressWarnings("PMD.AssignmentInOperand")
    private Rectangle2D getContactBestBox() {
        Collection<VisualContact> contacts = Hierarchy.getChildrenOfType(this, VisualContact.class);
        Rectangle2D bb = getContactExpandedBox(contacts);
        double x1 = bb.getMinX();
        double y1 = bb.getMinY();
        double x2 = bb.getMaxX();
        double y2 = bb.getMaxY();

        boolean westFirst = true;
        boolean northFirst = true;
        boolean eastFirst = true;
        boolean southFirst = true;

        for (VisualContact contact : contacts) {
            double x = contact.getX();
            double y = contact.getY();
            switch (contact.getDirection()) {
                case WEST -> {
                    if (westFirst) {
                        x1 = x + contactLength;
                    } else {
                        x1 = Math.max(x1, x + contactLength);
                    }
                    westFirst = false;
                }
                case NORTH -> {
                    if (northFirst) {
                        y1 = y + contactLength;
                    } else {
                        y1 = Math.max(y1, y + contactLength);
                    }
                    northFirst = false;
                }
                case EAST -> {
                    if (eastFirst) {
                        x2 = x - contactLength;
                    } else {
                        x2 = Math.min(x2, x - contactLength);
                    }
                    eastFirst = false;
                }
                case SOUTH -> {
                    if (southFirst) {
                        y2 = y - contactLength;
                    } else {
                        y2 = Math.min(y2, y - contactLength);
                    }
                    southFirst = false;
                }
            }
        }

        if (x1 > x2) {
            x1 = x2 = (x1 + x2) / 2;
        }
        if (y1 > y2) {
            y1 = y2 = (y1 + y2) / 2;
        }
        Rectangle2D maxBox = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
        return BoundingBoxHelper.union(bb, maxBox);
    }

    private Point2D getContactLinePosition(VisualContact vc) {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        return switch (vc.getDirection()) {
            case NORTH -> new Point2D.Double(vc.getX(), bb.getMinY());
            case EAST -> new Point2D.Double(bb.getMaxX(), vc.getY());
            case SOUTH -> new Point2D.Double(vc.getX(), bb.getMaxY());
            case WEST -> new Point2D.Double(bb.getMinX(), vc.getY());
        };
    }

    private void drawContactLines(DrawRequest r) {
        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class)) {
            Point2D p1 = vc.getPosition();
            Point2D p2 = getContactLinePosition(vc);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            Color colorisation = d.getColorisation();
            g.setStroke(new BasicStroke((float) CircuitSettings.getWireWidth()));
            g.setColor(ColorUtils.colorise(getForegroundColor(), colorisation));
            Line2D line = new Line2D.Double(p1, p2);
            g.draw(line);
        }
    }

    public Font getContactFont() {
        return NAME_FONT.deriveFont((float) CircuitSettings.getContactFontSize());
    }

    private GlyphVector getContactNameGlyphs(VisualContact vc, Supplier<String> getName) {
        if (Math.abs(CircuitSettings.getContactFontSize() - contactFontSize) > 0.001) {
            contactFontSize = CircuitSettings.getContactFontSize();
            contactNameGlyphs.clear();
        }
        GlyphVector gv = contactNameGlyphs.get(vc);
        if (gv == null) {
            gv = getContactFont().createGlyphVector(CONTEXT, getName.get());
            contactNameGlyphs.put(vc, gv);
        }
        return gv;
    }

    private void drawContactName(DrawRequest r, VisualContact vc) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        Color colorisation = d.getColorisation();
        Color color = vc.isInput() ? SignalCommonSettings.getInputColor() : SignalCommonSettings.getOutputColor();
        g.setColor(ColorUtils.colorise(color, colorisation));

        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        GlyphVector gv = getContactNameGlyphs(vc, () -> r.getModel().getMathName(vc));
        LineMetrics lineMetrics = getNameFont().getLineMetrics(vc.getName(), CONTEXT);
        double textWidth = gv.getVisualBounds().getWidth();
        double yCenterOffset = 0.2 * lineMetrics.getHeight();

        float x = 0.0f;
        float y = 0.0f;
        switch (vc.getDirection()) {
            case NORTH -> {
                x = (float) (-bb.getMinY() - labelMargin - textWidth);
                y = (float) (vc.getX() + yCenterOffset);
            }
            case EAST -> {
                x = (float) (bb.getMaxX() - labelMargin - textWidth);
                y = (float) (vc.getY() + yCenterOffset);
            }
            case SOUTH -> {
                x = (float) (-bb.getMaxY() + labelMargin);
                y = (float) (vc.getX() + yCenterOffset);
            }
            case WEST -> {
                x = (float) (bb.getMinX() + labelMargin);
                y = (float) (vc.getY() + yCenterOffset);
            }
        }
        g.drawGlyphVector(gv, x, y);
    }

    public void drawContactNames(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        AffineTransform savedTransform = g.getTransform();

        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class,
                contact -> (contact.getDirection() == Direction.WEST) || (contact.getDirection() == Direction.EAST))) {

            drawContactName(r, vc);
        }

        AffineTransform rotateTransform = new AffineTransform();
        rotateTransform.quadrantRotate(-1);
        g.transform(rotateTransform);

        for (VisualContact vc: Hierarchy.getChildrenOfType(this, VisualContact.class,
                contact -> (contact.getDirection() == Direction.NORTH) || (contact.getDirection() == Direction.SOUTH))) {

            drawContactName(r, vc);
        }

        g.setTransform(savedTransform);
    }

    public boolean getFanoutVisibility() {
        return CircuitSettings.getShowContactFanout();
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        if ((groupImpl != null) && (internalBB == null)) {
            internalBB = getContactBestBox();
        }
        if (internalBB != null) {
            return BoundingBoxHelper.copy(internalBB);
        }
        return super.getInternalBoundingBoxInLocalSpace();
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
        // Cache rendered text to better estimate the bounding box
        cacheRenderedText(r);
        drawOutline(r);
        drawRefinement(r);
        drawPivot(r);
        drawContactLines(r);
        drawContactNames(r);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
        // External decorations
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        d.decorate(g);
    }

    @Override
    public void drawOutline(DrawRequest r) {
        Decoration d = r.getDecoration();
        Graphics2D g = r.getGraphics();
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (bb != null) {
            g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
            g.fill(bb);
            g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
            setStroke(g);
            g.draw(bb);
        }
    }

    private void drawRefinement(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        File file = getReferencedComponent().getRefinementFile();
        if ((bb != null) && (file != null)) {
            double dx = VisualCommonSettings.getNodeSize() / 10;
            double dy = VisualCommonSettings.getNodeSize() / 10;
            double x = bb.getCenterX();
            double y = bb.getCenterY() + 0.3 * VisualCommonSettings.getNodeSize();
            double w = VisualCommonSettings.getNodeSize() / 20;
            double w2 = w / 2;
            Path2D p = new Path2D.Double();
            p.moveTo(x - dx - w, y + dy);
            p.lineTo(x - dx + w2, y + dy + w2 + w);
            p.lineTo(x - dx + w2, y + dy + w2);
            p.lineTo(x + dx + w2, y + dy + w2);
            p.lineTo(x + dx + w2, y);
            p.lineTo(x + dx - w2, y);
            p.lineTo(x + dx - w2, y + dy - w2);
            p.lineTo(x - dx + w2, y + dy - w2);
            p.lineTo(x - dx + w2, y + dy - w2 - w);
            p.closePath();
            g.fill(p);
        }
    }

    public void setStroke(Graphics2D g) {
        if (getIsEnvironment()) {
            g.setStroke(new BasicStroke((float) CircuitSettings.getBorderWidth(), BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 1.0f, new float[]{0.2f, 0.2f}, 0.0f));
        } else {
            g.setStroke(new BasicStroke((float) CircuitSettings.getBorderWidth()));
        }
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
            contactNameGlyphs.remove(node);
        }
        groupImpl.remove(node);
    }

    @Override
    public void add(Collection<? extends Node> nodes) {
        groupImpl.add(nodes);
        for (Node node : nodes) {
            if (node instanceof VisualContact) {
                ((VisualContact) node).addObserver(this);
            }
        }
    }

    @Override
    public void remove(Collection<? extends Node> nodes) {
        for (Node n : nodes) {
            remove(n);
        }
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
    public Node hitCustom(Point2D point) {
        Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
        for (Node node : getChildren()) {
            if (node instanceof VisualNode vn) {
                if (vn.hitTest(pointInLocalSpace)) {
                    return vn;
                }
            }
        }
        return hitTest(point) ? this : null;
    }

    @Override
    public void notify(StateEvent e) {
        if (e instanceof TransformChangedEvent t) {
            if (t.sender instanceof VisualContact vc) {

                AffineTransform at = t.sender.getTransform();
                double x = at.getTranslateX();
                double y = at.getTranslateY();
                Collection<VisualContact> contacts = Hierarchy.getChildrenOfType(this, VisualContact.class);
                Rectangle2D bb = getContactExpandedBox(contacts);
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

        if (e instanceof PropertyChangedEvent pc) {
            String propertyName = pc.getPropertyName();
            if (propertyName.equals(Contact.PROPERTY_NAME)
                    || propertyName.equals(Contact.PROPERTY_IO_TYPE)
                    || propertyName.equals(VisualContact.PROPERTY_DIRECTION)) {

                invalidateBoundingBox();
                contactNameGlyphs.clear();
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
        if (src instanceof VisualCircuitComponent srcComponent) {
            setIsEnvironment(srcComponent.getIsEnvironment());
        }
    }

    public void copyStylePreserveMapping(Stylable src) {
        CircuitComponent mathComponent = getReferencedComponent();
        String module = mathComponent.getModule();
        copyStyle(src);
        mathComponent.setModule(module);
    }

    @Override
    public String getLabel() {
        return getReferencedComponent().getModule();
    }

    @Override
    public void setLabel(String label) {
        getReferencedComponent().setModule(label);
        super.setLabel(label);
    }

    public Collection<VisualContact> getVisualContacts() {
        return Hierarchy.getChildrenOfType(this, VisualContact.class);
    }

    public List<VisualContact> getVisualInputs() {
        ArrayList<VisualContact> result = new ArrayList<>();
        for (VisualContact contact: getVisualContacts()) {
            if (contact.isInput()) {
                result.add(contact);
            }
        }
        return result;
    }

    public Collection<VisualContact> getVisualOutputs() {
        ArrayList<VisualContact> result = new ArrayList<>();
        for (VisualContact contact: getVisualContacts()) {
            if (contact.isOutput()) {
                result.add(contact);
            }
        }
        return result;
    }

    public VisualContact getFirstVisualInput() {
        VisualContact result = null;
        for (VisualContact contact: getVisualContacts()) {
            if (contact.isInput()) {
                result = contact;
                break;
            }
        }
        return result;
    }

    public VisualContact getFirstVisualOutput() {
        VisualContact result = null;
        for (VisualContact contact: getVisualContacts()) {
            if (contact.isOutput()) {
                result = contact;
                break;
            }
        }
        return result;
    }

    public VisualContact getMainVisualOutput() {
        VisualContact result = null;
        Collection<VisualContact> outputs = getVisualOutputs();
        if (outputs.size() == 1) {
            result = outputs.iterator().next();
        }
        return result;
    }

}
