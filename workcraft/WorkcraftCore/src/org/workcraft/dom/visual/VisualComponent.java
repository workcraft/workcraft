package org.workcraft.dom.visual;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.Identifier;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class VisualComponent extends VisualTransformableNode implements Dependent, Replicable, Drawable {

    public static final String PROPERTY_LABEL = "Label";
    public static final String PROPERTY_LABEL_POSITIONING = "Label positioning";
    public static final String PROPERTY_LABEL_COLOR = "Label color";
    public static final String PROPERTY_NAME_POSITIONING = "Name positioning";
    public static final String PROPERTY_NAME_COLOR = "Name color";
    public static final String PROPERTY_COLOR = "Color";
    public static final String PROPERTY_FILL_COLOR = "Fill color";

    public static final Font NAME_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
    public static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.ITALIC, 1);

    private final MathNode refNode;
    private Color foregroundColor = VisualCommonSettings.getBorderColor();
    private Color fillColor = VisualCommonSettings.getFillColor();

    private String label = "";
    private Positioning labelPositioning = VisualCommonSettings.getLabelPositioning();
    private RenderedText labelRenderedText = new RenderedText("", getLabelFont(), getLabelPositioning(), getLabelOffset());
    private Color labelColor = VisualCommonSettings.getLabelColor();

    private Positioning namePositioning = VisualCommonSettings.getNamePositioning();
    private RenderedText nameRenderedText = new RenderedText("", getNameFont(), getNamePositioning(), getNameOffset());
    private Color nameColor = VisualCommonSettings.getNameColor();

    private final HashSet<Replica> replicas = new HashSet<>();

    public VisualComponent(MathNode refNode) {
        this(refNode, true, true, true);
    }

    public VisualComponent(MathNode refNode, boolean hasColorProperties, boolean hasLabelProperties, boolean hasNameProperties) {
        super();
        this.refNode = refNode;

        if (refNode != null) {
            refNode.addObserver(observableStateImpl::sendNotification);
        }
        if (hasColorProperties) {
            addColorPropertyDeclarations();
        }
        if (hasLabelProperties) {
            addLabelPropertyDeclarations();
        }
        if (hasNameProperties) {
            addNamePropertyDeclarations();
        }
    }

    private void addColorPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Color.class, PROPERTY_COLOR,
                this::setForegroundColor, this::getForegroundColor).setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Color.class, PROPERTY_FILL_COLOR,
                this::setFillColor, this::getFillColor).setCombinable().setTemplatable());
    }

    private void addLabelPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(String.class, PROPERTY_LABEL,
                this::setLabel, this::getLabel).setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Positioning.class, PROPERTY_LABEL_POSITIONING,
                this::setLabelPositioning, this::getLabelPositioning).setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Color.class, PROPERTY_LABEL_COLOR,
                this::setLabelColor, this::getLabelColor).setCombinable().setTemplatable());
    }

    private void addNamePropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Positioning.class, PROPERTY_NAME_POSITIONING,
                this::setNamePositioning, this::getNamePositioning).setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Color.class, PROPERTY_NAME_COLOR,
                this::setNameColor, this::getNameColor).setCombinable().setTemplatable());
    }

    public Font getLabelFont() {
        return LABEL_FONT.deriveFont((float) VisualCommonSettings.getLabelFontSize());
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String value) {
        if (value == null) value = "";
        if (!value.equals(label)) {
            label = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_LABEL));
        }
    }

    public Positioning getLabelPositioning() {
        return labelPositioning;
    }

    public void setLabelPositioning(Positioning value) {
        if (labelPositioning != value) {
            labelPositioning = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_LABEL_POSITIONING));
        }
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(Color value) {
        if (labelColor != value) {
            labelColor = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_LABEL_COLOR));
        }
    }

    public Font getNameFont() {
        return NAME_FONT.deriveFont((float) VisualCommonSettings.getNameFontSize());
    }

    public Positioning getNamePositioning() {
        return namePositioning;
    }

    public void setNamePositioning(Positioning value) {
        if (namePositioning != value) {
            namePositioning = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_NAME_POSITIONING));
        }
    }

    public Color getNameColor() {
        return nameColor;
    }

    public void setNameColor(Color value) {
        if (!nameColor.equals(value)) {
            nameColor = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_NAME_COLOR));
        }
    }

    public boolean checkForegroundColor(Color value) {
        return getForegroundColor().equals(value);
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color value) {
        if (!checkForegroundColor(value)) {
            foregroundColor = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_COLOR));
        }
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color value) {
        if (!fillColor.equals(value)) {
            fillColor = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_FILL_COLOR));
        }
    }

    public MathNode getReferencedComponent() {
        return refNode;
    }

    @Override
    public Collection<MathNode> getMathReferences() {
        ArrayList<MathNode> result = new ArrayList<>();
        result.add(getReferencedComponent());
        return result;
    }

    public void centerPivotPoint(boolean horizontal, boolean vertical) {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (horizontal) {
            setX(getX() + bb.getCenterX());
        }
        if (vertical) {
            setY(getY() + bb.getCenterY());
        }
        for (Node node: getChildren()) {
            if (node instanceof VisualTransformableNode vc) {
                if (horizontal) {
                    vc.setX(vc.getX() - bb.getCenterX());
                }
                if (vertical) {
                    vc.setY(vc.getY() - bb.getCenterY());
                }
            }
        }
    }

    public boolean getLabelVisibility() {
        return VisualCommonSettings.getLabelVisibility();
    }

    public Point2D getOffset(Positioning positioning) {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        double xOffset = (positioning.xSign < 0) ? bb.getMinX() : (positioning.xSign > 0) ? bb.getMaxX() : bb.getCenterX();
        double yOffset = (positioning.ySign < 0) ? bb.getMinY() : (positioning.ySign > 0) ? bb.getMaxY() : bb.getCenterY();
        return new Point2D.Double(xOffset, yOffset);
    }

    public Point2D getLabelOffset() {
        return getOffset(getLabelPositioning());
    }

    public Alignment getLabelAlignment() {
        return switch (getLabelPositioning()) {
            case TOP_LEFT, LEFT, BOTTOM_LEFT -> Alignment.LEFT;
            case TOP, CENTER, BOTTOM -> Alignment.CENTER;
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> Alignment.RIGHT;
        };
    }

    protected void cacheLabelRenderedText(DrawRequest r) {
        cacheLabelRenderedText(getLabel(), getLabelFont(), getLabelPositioning(), getLabelOffset());
    }

    protected void cacheLabelRenderedText(String text, Font font, Positioning positioning, Point2D offset) {
        if ((labelRenderedText == null) || labelRenderedText.isDifferent(text, font, positioning, offset)) {
            labelRenderedText = new RenderedText(text, font, positioning, offset);
        }
    }

    protected void drawLabelInLocalSpace(DrawRequest r) {
        if (getLabelVisibility()) {
            cacheLabelRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(ColorUtils.colorise(getLabelColor(), d.getColorisation()));
            labelRenderedText.draw(g);
        }
    }

    protected void drawOutline(DrawRequest r) {
        Decoration d = r.getDecoration();
        Graphics2D g = r.getGraphics();
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (bb != null) {
            g.setStroke(new BasicStroke((float) VisualCommonSettings.getStrokeWidth()));
            g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
            g.draw(bb);
        }
    }

    protected void drawPivot(DrawRequest r) {
        Decoration d = r.getDecoration();
        Graphics2D g = r.getGraphics();
        if (d.getColorisation() != null) {
            float s2 = (float) VisualCommonSettings.getPivotSize() / 2;
            Path2D p = new Path2D.Double();
            p.moveTo(-s2, 0);
            p.lineTo(s2, 0);
            p.moveTo(0, -s2);
            p.lineTo(0, s2);
            g.setStroke(new BasicStroke((float) VisualCommonSettings.getPivotWidth()));
            g.draw(p);
        }
    }

    public boolean getNameVisibility() {
        return VisualCommonSettings.getNameVisibility();
    }

    public Point2D getNameOffset() {
        return getOffset(getNamePositioning());
    }

    protected void cacheNameRenderedText(DrawRequest r) {
        String name = null;
        MathModel mathModel = r.getModel().getMathModel();
        MathNode mathNode = getReferencedComponent();
        if ((this instanceof Replica) || VisualCommonSettings.getShowAbsolutePaths()) {
            name = mathModel.getNodeReference(mathNode);
        } else {
            name = mathModel.getName(mathNode);
        }
        if (name == null) {
            name = "";
        }
        cacheNameRenderedText(name, getNameFont(), getNamePositioning(), getNameOffset());
    }

    protected void cacheNameRenderedText(String text, Font font, Positioning positioning, Point2D offset) {
        text = Identifier.truncateNamespaceSeparator(text);
        if ((nameRenderedText == null) || nameRenderedText.isDifferent(text, font, positioning, offset)) {
            nameRenderedText = new RenderedText(text, font, positioning, offset);
        }
    }

    protected void drawNameInLocalSpace(DrawRequest r) {
        if (getNameVisibility()) {
            cacheNameRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(ColorUtils.colorise(getNameColor(), d.getColorisation()));
            nameRenderedText.draw(g);
        }
    }

    // This method is needed for VisualGroup to update the rendered text of its children
    // before they were drawn, which is necessary for computing their bounding boxes
    public void cacheRenderedText(DrawRequest r) {
        if (getLabelVisibility()) {
            cacheLabelRenderedText(r);
        }
        if (getNameVisibility()) {
            cacheNameRenderedText(r);
        }
    }

    public Shape getShape() {
        double size = VisualCommonSettings.getNodeSize() - VisualCommonSettings.getStrokeWidth();
        double pos = -0.5 * size;
        return new Rectangle2D.Double(pos, pos, size, size);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        Shape shape = getShape();
        g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);

        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        g.setStroke(new BasicStroke((float) VisualCommonSettings.getStrokeWidth()));
        g.draw(shape);

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    /*
     * The internal bounding box does not include the related label and name of the node
     */
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return getShape().getBounds2D();
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if ((labelRenderedText != null) && getLabelVisibility()) {
            bb = BoundingBoxHelper.union(bb, getLabelBoundingBox());
        }
        if ((nameRenderedText != null) && getNameVisibility()) {
            bb = BoundingBoxHelper.union(bb, getNameBoundingBox());
        }
        return bb;
    }

    public Rectangle2D getLabelBoundingBox() {
        if (labelRenderedText != null) {
            return labelRenderedText.getBoundingBox();
        } else {
            Rectangle2D box = getInternalBoundingBoxInLocalSpace();
            return new Rectangle2D.Double(box.getCenterX(), box.getCenterY(), 0.0, 0.0);
        }
    }

    public Rectangle2D getNameBoundingBox() {
        if (nameRenderedText != null) {
            return nameRenderedText.getBoundingBox();
        } else {
            Rectangle2D box = getInternalBoundingBoxInLocalSpace();
            return new Rectangle2D.Double(box.getCenterX(), box.getCenterY(), 0.0, 0.0);
        }
    }

    public Point2D getNameDrawPosition() {
        if (nameRenderedText != null) {
            return nameRenderedText.getDrawPosition();
        } else {
            Rectangle2D box = getInternalBoundingBoxInLocalSpace();
            return new Point2D.Double(box.getCenterX(), box.getCenterY());
        }
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return getInternalBoundingBoxInLocalSpace().contains(pointInLocalSpace);
    }

    @Override
    public void addReplica(Replica replica) {
        if (replica != null) {
            if (replicas.add(replica)) {
                replica.setMaster(this);
            }
        }
    }

    @Override
    public void removeReplica(Replica replica) {
        if (replica != null) {
            if (replicas.remove(replica)) {
                replica.setMaster(null);
            }
        }
    }

    @Override
    public Collection<Replica> getReplicas() {
        return Collections.unmodifiableCollection(replicas);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualComponent srcComponent) {
            setForegroundColor(srcComponent.getForegroundColor());
            setFillColor(srcComponent.getFillColor());
            setLabel(srcComponent.getLabel());
            setLabelColor(srcComponent.getLabelColor());
            setLabelPositioning(srcComponent.getLabelPositioning());
            setNameColor(srcComponent.getNameColor());
            setNamePositioning(srcComponent.getNamePositioning());
        }
    }

    @Override
    public void mixStyle(Stylable... srcs) {
        super.mixStyle(srcs);
        LinkedList<Color> foregroundColors = new LinkedList<>();
        LinkedList<Color> fillColors = new LinkedList<>();
        LinkedList<Color> nameColors = new LinkedList<>();
        LinkedList<Color> labelColors = new LinkedList<>();
        LinkedList<Positioning> namePositioning = new LinkedList<>();
        LinkedList<Positioning> labelPositioning = new LinkedList<>();
        for (Stylable src: srcs) {
            if (src instanceof VisualComponent srcComponent) {
                foregroundColors.add(srcComponent.getForegroundColor());
                fillColors.add(srcComponent.getFillColor());
                nameColors.add(srcComponent.getNameColor());
                labelColors.add(srcComponent.getLabelColor());
                namePositioning.add(srcComponent.getNamePositioning());
                labelPositioning.add(srcComponent.getLabelPositioning());
            }
        }
        setForegroundColor(ColorUtils.mix(foregroundColors));
        setFillColor(ColorUtils.mix(fillColors));
        setNameColor(ColorUtils.mix(nameColors));
        setLabelColor(ColorUtils.mix(labelColors));
        setNamePositioning(MixUtils.vote(namePositioning, Positioning.CENTER));
        setLabelPositioning(MixUtils.vote(labelPositioning, Positioning.CENTER));
    }

    @Override
    public void rotateClockwise() {
        setNamePositioning(getNamePositioning().rotateClockwise());
        setLabelPositioning(getLabelPositioning().rotateClockwise());
        super.rotateClockwise();
    }

    @Override
    public void rotateCounterclockwise() {
        setNamePositioning(getNamePositioning().rotateCounterclockwise());
        setLabelPositioning(getLabelPositioning().rotateCounterclockwise());
        super.rotateCounterclockwise();
    }

    @Override
    public void flipHorizontal() {
        setNamePositioning(getNamePositioning().flipHorizontal());
        setLabelPositioning(getLabelPositioning().flipHorizontal());
        super.flipHorizontal();
    }

    @Override
    public void flipVertical() {
        setNamePositioning(getNamePositioning().flipVertical());
        setLabelPositioning(getLabelPositioning().flipVertical());
        super.flipVertical();
    }

}
