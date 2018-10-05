package org.workcraft.dom.visual;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.CommonVisualSettings;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public abstract class VisualComponent extends VisualTransformableNode implements Dependent, Replicable, Drawable {
    public static final String PROPERTY_LABEL = "Label";
    public static final String PROPERTY_LABEL_POSITIONING = "Label positioning";
    public static final String PROPERTY_LABEL_COLOR = "Label color";
    public static final String PROPERTY_NAME_POSITIONING = "Name positioning";
    public static final String PROPERTY_NAME_COLOR = "Name color";
    public static final String PROPERTY_FOREGROUND_COLOR = "Foreground color";
    public static final String PROPERTY_FILL_COLOR = "Fill color";

    private MathNode refNode = null;
    protected double size = CommonVisualSettings.getNodeSize();
    protected double strokeWidth = CommonVisualSettings.getStrokeWidth();
    private Color foregroundColor = CommonVisualSettings.getBorderColor();
    private Color fillColor = CommonVisualSettings.getFillColor();

    private String label = "";
    private Positioning labelPositioning = CommonVisualSettings.getLabelPositioning();
    private RenderedText labelRenderedText = new RenderedText("", getLabelFont(), getLabelPositioning(), getLabelOffset());
    private Color labelColor = CommonVisualSettings.getLabelColor();

    private Positioning namePositioning = CommonVisualSettings.getNamePositioning();
    private RenderedText nameRenderedText = new RenderedText("", getNameFont(), getNamePositioning(), getNameOffset());
    private Color nameColor = CommonVisualSettings.getNameColor();

    private final HashSet<Replica> replicas = new HashSet<>();

    public VisualComponent(MathNode refNode) {
        this(refNode, true, true, true);
    }

    public VisualComponent(MathNode refNode, boolean hasColorProperties, boolean hasLabelProperties, boolean hasNameProperties) {
        super();
        this.refNode = refNode;

        if (refNode instanceof ObservableState) {
            ((ObservableState) refNode).addObserver(e -> observableStateImpl.sendNotification(e));
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
        addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Color>(
                this, PROPERTY_FOREGROUND_COLOR, Color.class, true, true, true) {
            protected void setter(VisualComponent object, Color value) {
                object.setForegroundColor(value);
            }
            protected Color getter(VisualComponent object) {
                return object.getForegroundColor();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Color>(
                this, PROPERTY_FILL_COLOR, Color.class, true, true, true) {
            protected void setter(VisualComponent object, Color value) {
                object.setFillColor(value);
            }
            protected Color getter(VisualComponent object) {
                return object.getFillColor();
            }
        });
    }

    private void addLabelPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualComponent, String>(
                this, PROPERTY_LABEL, String.class, true, true, true) {
            protected void setter(VisualComponent object, String value) {
                object.setLabel(value);
            }
            protected String getter(VisualComponent object) {
                return object.getLabel();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Positioning>(
                this, PROPERTY_LABEL_POSITIONING, Positioning.class, true, true, true) {
            protected void setter(VisualComponent object, Positioning value) {
                object.setLabelPositioning(value);
            }
            protected Positioning getter(VisualComponent object) {
                return object.getLabelPositioning();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Color>(
                this, PROPERTY_LABEL_COLOR, Color.class, true, true, true) {
            protected void setter(VisualComponent object, Color value) {
                object.setLabelColor(value);
            }
            protected Color getter(VisualComponent object) {
                return object.getLabelColor();
            }
        });
    }

    private void addNamePropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Positioning>(
                this, PROPERTY_NAME_POSITIONING, Positioning.class, true, true, true) {
            protected void setter(VisualComponent object, Positioning value) {
                object.setNamePositioning(value);
            }
            protected Positioning getter(VisualComponent object) {
                return object.getNamePositioning();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Color>(
                this, PROPERTY_NAME_COLOR, Color.class, true, true, true) {
            protected void setter(VisualComponent object, Color value) {
                object.setNameColor(value);
            }
            protected Color getter(VisualComponent object) {
                return object.getNameColor();
            }
        });
    }

    public Font getLabelFont() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(0.5f);
    }

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
        return new Font(Font.SANS_SERIF, Font.ITALIC, 1).deriveFont(0.5f);
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

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color value) {
        if (!foregroundColor.equals(value)) {
            foregroundColor = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_FOREGROUND_COLOR));
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

    @Override
    public Point2D getCenterInLocalSpace() {
        return new Point2D.Double(0, 0);
    }

    public void centerPivotPoint(boolean horisontal, boolean vertical) {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (horisontal) {
            setX(getX() + bb.getCenterX());
        }
        if (vertical) {
            setY(getY() + bb.getCenterY());
        }
        for (Node node: getChildren()) {
            if (node instanceof VisualTransformableNode) {
                VisualTransformableNode vc = (VisualTransformableNode) node;
                if (horisontal) {
                    vc.setX(vc.getX() - bb.getCenterX());
                }
                if (vertical) {
                    vc.setY(vc.getY() - bb.getCenterY());
                }
            }
        }
    }

    public boolean getLabelVisibility() {
        return CommonVisualSettings.getLabelVisibility();
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
        switch (getLabelPositioning()) {
        case TOP_LEFT:
        case LEFT:
        case BOTTOM_LEFT:
            return Alignment.LEFT;
        case TOP:
        case CENTER:
        case BOTTOM:
            return Alignment.CENTER;
        case TOP_RIGHT:
        case RIGHT:
        case BOTTOM_RIGHT:
            return Alignment.RIGHT;
        default:
            return Alignment.LEFT;
        }
    }

    protected boolean cacheLabelRenderedText(DrawRequest r) {
        return cacheLabelRenderedText(getLabel(), getLabelFont(), getLabelPositioning(), getLabelOffset());
    }

    protected boolean cacheLabelRenderedText(String text, Font font, Positioning positioning, Point2D offset) {
        if (labelRenderedText.isDifferent(text, font, positioning, offset)) {
            labelRenderedText = new RenderedText(text, font, positioning, offset);
            return true;
        }
        return false;
    }

    protected void drawLabelInLocalSpace(DrawRequest r) {
        if (getLabelVisibility() && (labelRenderedText != null) && !labelRenderedText.isEmpty()) {
            cacheLabelRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(Coloriser.colorise(getLabelColor(), d.getColorisation()));
            labelRenderedText.draw(g, getLabelAlignment());
        }
    }

    protected void drawOutline(DrawRequest r) {
        Decoration d = r.getDecoration();
        Graphics2D g = r.getGraphics();
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (bb != null) {
            g.setStroke(new BasicStroke((float) strokeWidth));
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
            g.draw(bb);
        }
    }

    protected void drawPivot(DrawRequest r) {
        Decoration d = r.getDecoration();
        Graphics2D g = r.getGraphics();
        if (d.getColorisation() != null) {
            float s2 = (float) CommonVisualSettings.getPivotSize() / 2;
            Path2D p = new Path2D.Double();
            p.moveTo(-s2, 0);
            p.lineTo(s2, 0);
            p.moveTo(0, -s2);
            p.lineTo(0, s2);
            g.setStroke(new BasicStroke((float) CommonVisualSettings.getPivotWidth()));
            g.draw(p);
        }
    }

    public boolean getNameVisibility() {
        return CommonVisualSettings.getNameVisibility();
    }

    public Point2D getNameOffset() {
        return getOffset(getNamePositioning());
    }

    protected boolean cacheNameRenderedText(DrawRequest r) {
        String name = null;
        MathModel mathModel = r.getModel().getMathModel();
        MathNode mathNode = getReferencedComponent();
        if ((this instanceof Replica) || CommonEditorSettings.getShowAbsolutePaths()) {
            name = mathModel.getNodeReference(mathNode);
        } else {
            name = mathModel.getName(mathNode);
        }
        if (name == null) {
            name = "";
        }
        return cacheNameRenderedText(name, getNameFont(), getNamePositioning(), getNameOffset());
    }

    protected boolean cacheNameRenderedText(String text, Font font, Positioning positioning, Point2D offset) {
        if (nameRenderedText.isDifferent(text, font, positioning, offset)) {
            nameRenderedText = new RenderedText(text, font, positioning, offset);
            return true;
        }
        return false;
    }

    protected void drawNameInLocalSpace(DrawRequest r) {
        if (getNameVisibility() && (nameRenderedText != null) && !nameRenderedText.isEmpty()) {
            cacheNameRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(Coloriser.colorise(getNameColor(), d.getColorisation()));
            nameRenderedText.draw(g);
        }
    }

    // This method is needed for VisualGroup to update the rendered text of its children
    // before they were drawn, which is necessary for computing their bounding boxes
    public void cacheRenderedText(DrawRequest r) {
        cacheLabelRenderedText(r);
        cacheNameRenderedText(r);
    }

    /*
     * The internal bounding box does not include the related label and name of the node
     */
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return new Rectangle2D.Double(-size / 2, -size / 2, size, size);
    }

    public Rectangle2D getInternalBoundingBox() {
        return transformToParentSpace(getInternalBoundingBoxInLocalSpace());
    }

    public Rectangle2D getInternalBoundingBoxInRootSpace() {
        Point2D p = getRootSpaceTranslation();
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        return new Rectangle2D.Double(bb.getX() + p.getX(), bb.getY() + p.getY(), bb.getWidth(), bb.getHeight());
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (getLabelVisibility()) {
            bb = BoundingBoxHelper.union(bb, getLabelBoundingBox());
        }
        if (getNameVisibility()) {
            bb = BoundingBoxHelper.union(bb, getNameBoundingBox());
        }
        return bb;
    }

    public Rectangle2D getLabelBoundingBox() {
        if ((labelRenderedText != null) && !labelRenderedText.isEmpty()) {
            return labelRenderedText.getBoundingBox();
        } else {
            return null;
        }
    }

    public Rectangle2D getNameBoundingBox() {
        if ((nameRenderedText != null) && !nameRenderedText.isEmpty()) {
            return nameRenderedText.getBoundingBox();
        } else {
            return null;
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
        if (src instanceof VisualComponent) {
            VisualComponent srcComponent = (VisualComponent) src;
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
        String label = "";
        for (Stylable src: srcs) {
            if (src instanceof VisualComponent) {
                VisualComponent srcComponent = (VisualComponent) src;
                foregroundColors.add(srcComponent.getForegroundColor());
                fillColors.add(srcComponent.getFillColor());
                nameColors.add(srcComponent.getNameColor());
                labelColors.add(srcComponent.getLabelColor());
                namePositioning.add(srcComponent.getLabelPositioning());
                labelPositioning.add(srcComponent.getLabelPositioning());
                if (srcComponent.getLabel() != null) {
                    label = label + "|" + srcComponent.getLabel();
                }
            }
        }
        setForegroundColor(Coloriser.mix(foregroundColors));
        setFillColor(Coloriser.mix(fillColors));
        setNameColor(Coloriser.mix(nameColors));
        setLabelColor(Coloriser.mix(labelColors));
        setNamePositioning(MixUtils.vote(namePositioning, Positioning.CENTER));
        setLabelPositioning(MixUtils.vote(labelPositioning, Positioning.CENTER));
        //setLabel(label);
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
