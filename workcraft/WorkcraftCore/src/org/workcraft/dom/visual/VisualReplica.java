package org.workcraft.dom.visual;

import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VisualReplica extends VisualTransformableNode implements Replica, Drawable {

    public static final String PROPERTY_NAME_COLOR = "Name color";

    public static final Font NAME_FONT = new Font(Font.SANS_SERIF, Font.ITALIC, 1);

    private RenderedText nameRenderedText = new RenderedText("", getNameFont(), getNamePositioning(), getNameOffset());
    private Color nameColor = VisualCommonSettings.getNameColor();

    private VisualComponent master = null;

    public VisualReplica() {
        super();
        addPropertyDeclaration(new PropertyDeclaration<>(Color.class, PROPERTY_NAME_COLOR,
                this::setNameColor, this::getNameColor).setCombinable().setTemplatable());
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

    public Positioning getNamePositioning() {
        return Positioning.CENTER;
    }

    public Font getNameFont() {
        return NAME_FONT.deriveFont((float) VisualCommonSettings.getNameFontSize());
    }

    public Point2D getNameOffset() {
        return new Point2D.Double(0.0, 0.0);
    }

    private void cacheNameRenderedText(DrawRequest r) {
        String name = null;
        MathModel mathModel = r.getModel().getMathModel();
        if (master != null) {
            MathNode mathNode = master.getReferencedComponent();
            name = mathModel.getNodeReference(mathNode);
        }

        if (name == null) {
            name = "";
        }
        cacheNameRenderedText(name, getNameFont(), getNamePositioning(), getNameOffset());
    }

    protected void cacheNameRenderedText(String text, Font font, Positioning positioning, Point2D offset) {
        if ((nameRenderedText == null) || nameRenderedText.isDifferent(text, font, positioning, offset)) {
            nameRenderedText = new RenderedText(text, font, positioning, offset);
        }
    }

    protected void drawNameInLocalSpace(DrawRequest r) {
        cacheNameRenderedText(r);
        if (!nameRenderedText.isEmpty()) {
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(ColorUtils.colorise(getNameColor(), d.getColorisation()));
            nameRenderedText.draw(g);
        }
    }

    // This method is needed for VisualGroup to update the rendered text of its children
    // before they were drawn, which is necessary for computing their bounding boxes
    public void cacheRenderedText(DrawRequest r) {
        cacheNameRenderedText(r);
    }

    /*
     * The internal bounding box does not include the related label and name of the node
     */
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return BoundingBoxHelper.expand(getNameBoundingBox(), 0.2, 0.2);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        return BoundingBoxHelper.union(bb, getNameBoundingBox());
    }

    public Rectangle2D getNameBoundingBox() {
        if ((nameRenderedText != null) && !nameRenderedText.isEmpty()) {
            return nameRenderedText.getBoundingBox();
        } else {
            return new Rectangle2D.Double();
        }
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return getInternalBoundingBoxInLocalSpace().contains(pointInLocalSpace);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualReplica) {
            VisualReplica srcReplica = (VisualReplica) src;
            setNameColor(srcReplica.getNameColor());
        }
    }

    @Override
    public void setMaster(VisualComponent value) {
        if (master != value) {
            if (master != null) {
                master.removeReplica(this);
            }
            master = value;
            if (master != null) {
                master.addReplica(this);
            }
        }
    }

    @Override
    public VisualComponent getMaster() {
        return master;
    }

    @Override
    public void draw(DrawRequest r) {
        cacheRenderedText(r);  // needed to better estimate the bounding box
        drawNameInLocalSpace(r);
    }

    public MathNode getReferencedComponent() {
        return master == null ? null : master.getReferencedComponent();
    }

}
